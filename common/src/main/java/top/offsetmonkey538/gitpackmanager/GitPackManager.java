package top.offsetmonkey538.gitpackmanager;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jspecify.annotations.Nullable;
import top.offsetmonkey538.gitpackmanager.command.GitPackManagerCommand;
import top.offsetmonkey538.gitpackmanager.config.ConfigHandler;
import top.offsetmonkey538.gitpackmanager.config.ModConfig;
import top.offsetmonkey538.gitpackmanager.exception.GitPackManagerException;
import top.offsetmonkey538.gitpackmanager.handler.DataPackHandler;
import top.offsetmonkey538.gitpackmanager.handler.GitHandler;
import top.offsetmonkey538.gitpackmanager.handler.ResourcePackHandler;
import top.offsetmonkey538.gitpackmanager.networking.MainHttpHandler;
import top.offsetmonkey538.gitpackmanager.platform.PlatformMain;
import top.offsetmonkey538.gitpackmanager.platform.PlatformServerProperties;
import top.offsetmonkey538.gitpackmanager.platform.PlatformText;
import top.offsetmonkey538.gitpackmanager.utils.StringUtils;
import top.offsetmonkey538.meshlib.common.api.router.HttpRouter;
import top.offsetmonkey538.meshlib.common.api.router.HttpRouterRegistry;
import top.offsetmonkey538.monkeylib538.common.api.command.ConfigCommandApi;
import top.offsetmonkey538.monkeylib538.common.api.lifecycle.ServerLifecycleApi;
import top.offsetmonkey538.monkeylib538.common.api.platform.LoaderUtil;
import top.offsetmonkey538.monkeylib538.common.api.telemetry.TelemetryRegistry;
import top.offsetmonkey538.offsetutils538.api.config.ConfigHolder;
import top.offsetmonkey538.offsetutils538.api.config.ConfigManager;
import top.offsetmonkey538.offsetutils538.api.log.OffsetLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static top.offsetmonkey538.offsetutils538.api.text.ArgReplacer.replaceArgs;

public final class GitPackManager {
    private GitPackManager() {

    }

    public static final String MOD_ID = "git-pack-manager";
    public static final OffsetLogger LOGGER = OffsetLogger.create(MOD_ID);

    public static final Path DATA_FOLDER =  LoaderUtil.getConfigDir().resolve(MOD_ID).resolve(".packs");
    public static final Path GIT_FOLDER = DATA_FOLDER.resolve("git");

    public static final Path RESOURCEPACK_FOLDER =  DATA_FOLDER.resolve("resource-pack");
    public static final Path DATAPACK_FOLDER =  DATA_FOLDER.resolve("data-pack");

    public static final Path RESOURCEPACK_OUTPUT_FOLDER = RESOURCEPACK_FOLDER.resolve("output");

    public static final Pattern RESOURCEPACK_NAME_PATTERN = Pattern.compile("\\d+-");
    public static final UUID RESOURCEPACK_UUID = UUID.fromString("60ab8dc7-08d1-4f5f-a9a8-9a01d048b7b9");

    public static final Map<String, String> STATIC_PLACEHOLDERS = Map.of("{packUpdateCommand}", "/git-pack-manager request-pack");

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat(MOD_ID + "-%d").build());

    private static final List<Component> MESSAGE_QUEUE = new ArrayList<>();

    private static final Component MESSAGE_QUEUE_EMPTY_MESSAGE;
    static {
        try {
            MESSAGE_QUEUE_EMPTY_MESSAGE = Component.text("TODO");
            // TODO: Use MiniMessage: MESSAGE_QUEUE_EMPTY_MESSAGE = TextFormattingApi.styleText("Admin message queue can be emptied using the &{hoverText,'Click to run','&{runCommand,'/gh-rp-manager reset-admin-message-queue','[/gh-rp-manager reset-admin-message-queue]'}'} command.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ConfigHolder<ModConfig> config = ConfigHolder.create(ModConfig::new, LOGGER);

    public static @Nullable ResourcePackHandler resourcePackHandler = null;

    private static boolean disabled;

    public static void initialize() {
        TelemetryRegistry.register(MOD_ID);

        // Have to run all this stuff (definitely initializing config at least) after other mods have initialized. Namely, after MESH-Lib has its rule serialization stuff to the jankson event
        ServerLifecycleApi.STARTED.listen(() ->  {
            addLogToAdminListeners();
            LoaderUtil.sendMessagesToAdminsOnJoin(() -> {
                if (MESSAGE_QUEUE.isEmpty()) return new Component[]{};

                final Component[] result = new Component[MESSAGE_QUEUE.size() + 1];
                MESSAGE_QUEUE.toArray(result);
                result[result.length - 1] = MESSAGE_QUEUE_EMPTY_MESSAGE;
                return result;
            });

            // config should be initialized after the error listeners
            ConfigManager.init(config);

            GitPackManagerCommand.register();
            ConfigCommandApi.registerConfigCommand(
                    config,
                    () -> disabled = ConfigHandler.handleConfig(),
                    "gh-rp-manager", "config"
            );

            disabled = ConfigHandler.handleConfig();

            try {
                createFolderStructure();
            } catch (GitPackManagerException e) {
                LOGGER.error("Failed to create folder structure!", e);
            }

            updatePack(UpdateType.RESTART, true);
        });

        HttpRouterRegistry.HTTP_ROUTER_REGISTRATION_EVENT.listen(registry -> registry.register(
                MOD_ID, new HttpRouter(
                        config.get().serverInfo.routingRule,
                        new MainHttpHandler()
                )
        ));
    }

    private static void addLogToAdminListeners() {
        LOGGER.addListener(OffsetLogger.LogLevel.ERROR, createLogToAdminListener(NamedTextColor.RED));
        LOGGER.addListener(OffsetLogger.LogLevel.WARN, createLogToAdminListener(NamedTextColor.YELLOW));
    }

    private static OffsetLogger.LogListener createLogToAdminListener(final TextColor textColor) {
        return (message, error) -> {
            final Component text = Component
                    .text(replaceArgs("[%s] %s", MOD_ID, message))
                    .style(style -> style.color(textColor))
                    .style(error == null ? style -> {} : style -> style.hoverEvent(HoverEvent.showText(Component.text(ExceptionUtils.getRootCauseMessage(error)))));

            PlatformMain.INSTANCE.sendMessageToAdmins(text);
            MESSAGE_QUEUE.addLast(text);
        };
    }

    private static void createFolderStructure() throws GitPackManagerException {
        try {
            Files.createDirectories(RESOURCEPACK_OUTPUT_FOLDER);
            Files.createDirectories(DATAPACK_FOLDER);
            Files.createDirectories(GIT_FOLDER);
        } catch (IOException e) {
            throw new GitPackManagerException("Failed to create directory!", e);
        }
    }

    public static CompletableFuture<Void> updatePack(final UpdateType updateType, final boolean onSameThread) {
        if (!onSameThread) return CompletableFuture.runAsync(() -> updatePack(updateType), EXECUTOR);

        updatePack(updateType);
        return CompletableFuture.completedFuture(null);
    }

    private static void updatePack(final UpdateType updateType) {
        if (updateType != UpdateType.RESTART) {
            LOGGER.debug("Clearing admin message queue before updating after a restart...");
            MESSAGE_QUEUE.clear();
        }

        if (disabled) {
            LOGGER.warn("Skipping pack updating because mod is disabled!");
            return;
        }

        LOGGER.info("Updating packs...");

        if (updateType == UpdateType.COMMAND_FORCE) {
            LOGGER.warn("Forced pack update! Deleting data directory and continuing...");
            try {
                FileUtils.deleteDirectory(DATA_FOLDER.toFile());
            } catch (IOException e) {
                LOGGER.error("Failed to delete directory!", e);
                return;
            }
        }

        try {
            createFolderStructure();
        } catch (GitPackManagerException e) {
            LOGGER.error("Failed to create folder structure!", e);
        }

        // Git stuff
        final GitHandler gitHandler = new GitHandler();

        LOGGER.info("Updating git repository...");
        boolean failed = false;
        try {
            gitHandler.updateRepositoryAndGenerateCommitProperties();
        } catch (GitPackManagerException e) {
            LOGGER.error("Failed to update git repository!", e);
            failed = true;
        }
        if (!failed) LOGGER.info("Successfully updated git repository!");

        if (config.get().resourcePackProvider.enabled) {
            LOGGER.info("");
            LOGGER.info("Updating resource pack...");
            updateResourcePack(gitHandler, updateType, failed);
            LOGGER.info("Resource pack updated!");
        }
        if (config.get().dataPackProvider.enabled) {
            LOGGER.info("");
            LOGGER.info("Updating data pack...");
            updateDataPack(gitHandler, updateType, failed);
            LOGGER.info("Data pack updated!");
        }
    }

    private static void updateResourcePack(final GitHandler gitHandler, final UpdateType updateType, boolean updateFailed) {
        // Get the location of the old pack, if it exists.
        final String oldResourcePackName = getOldResourcePackName();
        final Path oldResourcePackPath = oldResourcePackName == null ? null : RESOURCEPACK_OUTPUT_FOLDER.resolve(oldResourcePackName);

        // Check if pack was updated
        final boolean wasUpdated =
                gitHandler.getChangedFiles().map(changes -> changes.stream().anyMatch(it -> it.startsWith(config.get().resourcePackProvider.getRootLocation()))).orElse(true)
                        || oldResourcePackPath == null
                        || !oldResourcePackPath.toFile().exists();
        if (!wasUpdated) LOGGER.info("Pack hasn't changed since last update. Skipping new pack generation.");

        // Generate pack
        resourcePackHandler = new ResourcePackHandler();

        LOGGER.info("Getting pack location...");
        boolean failed = false;
        try {
            resourcePackHandler.generatePack(wasUpdated, oldResourcePackPath, oldResourcePackName);
        } catch (GitPackManagerException e) {
            LOGGER.error("Failed to generate pack!", e);
            failed = updateFailed = true;
        }
        if (!failed) LOGGER.info("Pack location is '%s'!", resourcePackHandler.getOutputPackPath().toAbsolutePath());


        // Update server.properties file.
        try {
            PlatformServerProperties.INSTANCE.updatePackProperties(resourcePackHandler);
        } catch (GitPackManagerException e) {
            LOGGER.error("Failed to update server.properties file!", e);
        }

        // Generate placeholder map
        final Map<String, String> placeholders = generatePlaceholders(gitHandler, resourcePackHandler, updateType, "resource", wasUpdated);

        // Send chat message
        try {
            if (!failed) sendUpdateMessage(config.get().resourcePackProvider.updateMessage, wasUpdated, placeholders);
        } catch (GitPackManagerException e) {
            LOGGER.error("Failed to send update message for resource pack in chat!", e);
        }

        // Trigger webhooks
        if (!wasUpdated) {
            LOGGER.info("Not sending webhook because pack was not updated.");
            return;
        }
        if (!updateFailed) try {
            config.get().resourcePackProvider.successWebhook.trigger(true, placeholders, updateType);
        } catch (GitPackManagerException e) {
            LOGGER.error("Failed to trigger success webhook!", e);
        }
        else try {
            config.get().resourcePackProvider.failWebhook.trigger(false, placeholders, updateType);
        } catch (GitPackManagerException e) {
            LOGGER.error("Failed to trigger fail webhook!", e);
        }
    }

    private static void updateDataPack(final GitHandler gitHandler, final UpdateType updateType, boolean updateFailed) {
        // Check if pack was updated
        final boolean wasUpdated = gitHandler.getChangedFiles().map(changes -> changes.stream().anyMatch(it -> it.startsWith(config.get().dataPackProvider.getRootLocation()))).orElse(true);
        if (!wasUpdated) {
            LOGGER.info("Pack hasn't changed since last update. Datapack processing will be skipped.");
            return;
        }

        // Generate pack
        final DataPackHandler dataPackHandler = new DataPackHandler();

        LOGGER.info("Getting pack location...");
        try {
            dataPackHandler.generatePack();
        } catch (GitPackManagerException e) {
            LOGGER.error("Failed to generate pack!", e);
            updateFailed = true;
        }

        // Refresh datapack list
        PlatformMain.INSTANCE.refreshDatapacks();


        // Generate placeholder map
        final Map<String, String> placeholders = generatePlaceholders(gitHandler, null, updateType, "data", true);

        // Send chat message
        try {
            sendUpdateMessage(config.get().dataPackProvider.updateMessage, true, placeholders, true);
        } catch (GitPackManagerException e) {
            LOGGER.error("Failed to send update message for datapack in chat!", e);
        }

        // Trigger webhooks
        if (!updateFailed) try {
            config.get().dataPackProvider.successWebhook.trigger(true, placeholders, updateType);
        } catch (GitPackManagerException e) {
            LOGGER.error("Failed to trigger success webhook!", e);
        }
        else try {
            config.get().dataPackProvider.failWebhook.trigger(false, placeholders, updateType);
        } catch (GitPackManagerException e) {
            LOGGER.error("Failed to trigger fail webhook!", e);
        }
    }

    private static void sendUpdateMessage(final String[] updateMessage, boolean wasUpdated, final Map<String, String> placeholders) throws GitPackManagerException {
        sendUpdateMessage(updateMessage, wasUpdated, placeholders, false);
    }

    private static void sendUpdateMessage(final String[] updateMessage, boolean wasUpdated, final Map<String, String> placeholders, boolean adminsOnly) throws GitPackManagerException {
        if (!wasUpdated) {
            LOGGER.info("Not sending chat message because pack was not updated.");
            return;
        }

        for (final Component line : createUpdateMessage(updateMessage, placeholders))
            PlatformText.INSTANCE.sendUpdateMessage(line, adminsOnly);
    }

    public static Component[] createUpdateMessage(final String[] updateMessage, final Map<String, String> placeholders) throws GitPackManagerException {
        final Component[] result = new Component[updateMessage.length];

        for (int lineIndex = 0; lineIndex < updateMessage.length; lineIndex++) {
            final String line = StringUtils.replacePlaceholders(updateMessage[lineIndex], placeholders, true, false).replace("\\n", "\n");

            try {
                result[lineIndex] = Component.text("TODO (UNFORMATTED): " + line);
                // TODO: use MiniMessage: result[lineIndex] = TextFormattingApi.styleText(line);
            } catch (Exception e) {
                throw new GitPackManagerException("Failed to style update message at line %s!", e, lineIndex);
            }
        }

        return result;
    }

    public static Map<String, String> generatePlaceholders(final GitHandler gitHandler, final @Nullable ResourcePackHandler resourcePackHandler, final UpdateType updateType, final String packType, final boolean wasUpdated) {
        final Map<String, String> placeholders = new HashMap<>();

        if (gitHandler.getCommitProperties() != null) placeholders.putAll(gitHandler.getCommitProperties().toPlaceholdersMap());
        if (resourcePackHandler != null) placeholders.put("{downloadUrl}", config.get().getPackUrl(Objects.requireNonNull(resourcePackHandler.getOutputPackName())));
        placeholders.putAll(STATIC_PLACEHOLDERS);
        placeholders.put("{packType}", packType);
        placeholders.put("{updateType}", updateType.name());
        placeholders.put("{wasUpdated}", String.valueOf(wasUpdated));
        LOGGER.info("Placeholders: %s", placeholders);

        return placeholders;
    }

    public static void clearAdminMessageQueue() {
        MESSAGE_QUEUE.clear();
    }

    private static @Nullable String getOldResourcePackName() {
        final String oldPackUrl = PlatformServerProperties.INSTANCE.getResourcePackUrl();
        if (oldPackUrl == null) return null;

        int nameStartIndex = oldPackUrl.lastIndexOf('/');
        if (nameStartIndex == -1) return null;

        return oldPackUrl.substring(nameStartIndex + 1);
    }

    public enum UpdateType {
        RESTART,
        WEBHOOK,
        COMMAND,
        COMMAND_FORCE
    }
}
