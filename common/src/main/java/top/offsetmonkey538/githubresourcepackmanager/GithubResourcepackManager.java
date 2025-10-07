package top.offsetmonkey538.githubresourcepackmanager;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.offsetmonkey538.githubresourcepackmanager.command.GitPackManagerCommand;
import top.offsetmonkey538.githubresourcepackmanager.config.ModConfig;
import top.offsetmonkey538.githubresourcepackmanager.config.ConfigHandler;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.githubresourcepackmanager.handler.DataPackHandler;
import top.offsetmonkey538.githubresourcepackmanager.handler.GitHandler;
import top.offsetmonkey538.githubresourcepackmanager.handler.ResourcePackHandler;
import top.offsetmonkey538.githubresourcepackmanager.networking.MainHttpHandler;
import top.offsetmonkey538.githubresourcepackmanager.platform.*;
import top.offsetmonkey538.githubresourcepackmanager.utils.StringUtils;
import top.offsetmonkey538.meshlib.api.router.HttpRouter;
import top.offsetmonkey538.meshlib.api.router.HttpRouterRegistry;
import top.offsetmonkey538.meshlib.api.router.rule.HttpRule;
import top.offsetmonkey538.meshlib.impl.router.rule.DomainHttpRule;
import top.offsetmonkey538.monkeylib538.api.command.ConfigCommandApi;
import top.offsetmonkey538.monkeylib538.api.log.MonkeyLibLogger;
import top.offsetmonkey538.monkeylib538.api.text.MonkeyLibStyle;
import top.offsetmonkey538.monkeylib538.api.text.MonkeyLibText;
import top.offsetmonkey538.monkeylib538.api.text.TextFormattingApi;
import top.offsetmonkey538.offsetconfig538.api.config.ConfigHolder;
import top.offsetmonkey538.offsetconfig538.api.config.ConfigManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public final class GithubResourcepackManager {
    private GithubResourcepackManager() {

    }

    public static final String MOD_ID = "github-resourcepack-manager";
    public static final MonkeyLibLogger LOGGER = MonkeyLibLogger.create(MOD_ID);
    public static final String MOD_URI = "gh-rp-manager";

    public static final Path DATA_FOLDER =  PlatformMain.INSTANCE.getConfigDir().resolve(".packs");
    public static final Path GIT_FOLDER = DATA_FOLDER.resolve("git");

    public static final Path RESOURCEPACK_FOLDER =  DATA_FOLDER.resolve("resource-pack");
    public static final Path DATAPACK_FOLDER =  DATA_FOLDER.resolve("data-pack");

    public static final Path RESOURCEPACK_OUTPUT_FOLDER = RESOURCEPACK_FOLDER.resolve("output");

    public static final Pattern RESOURCEPACK_NAME_PATTERN = Pattern.compile("\\d+-");
    public static final UUID RESOURCEPACK_UUID = UUID.fromString("60ab8dc7-08d1-4f5f-a9a8-9a01d048b7b9");

    public static final Map<String, String> STATIC_PLACEHOLDERS = Map.of("{packUpdateCommand}", "/gh-rp-manager request-pack");

    private static final List<MonkeyLibText> MESSAGE_QUEUE = new ArrayList<>();

    @SuppressWarnings("NotNullFieldNotInitialized")
    public static @NotNull ConfigHolder<ModConfig> config;

    public static ResourcePackHandler resourcePackHandler;

    private static boolean disabled;

    public static void initialize() {
        addLogToAdminListeners();
        PlatformMain.INSTANCE.registerSendMessageQueueOnAdminJoin(MESSAGE_QUEUE);

        // config should be initialized after the error listeners
        config = ConfigManager.INSTANCE.init(ConfigHolder.create(ModConfig::new, LOGGER::error));

        GitPackManagerCommand.register();
        ConfigCommandApi.registerConfigCommand(
                config,
                () -> disabled = ConfigHandler.handleConfig(),
                "gh-rp-manager", "config"
        );

        disabled = ConfigHandler.handleConfig();

        try {
            createFolderStructure();
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to create folder structure!", e);
        }

        HttpRouterRegistry.INSTANCE.register(MOD_URI, new HttpRouter(
                new HttpRule<>() {
                    @Override
                    public String getType() {
                        return "2hartqwf";
                    }

                    @Override
                    public Object getData() {
                        return new Object();
                    }

                    @Override
                    public boolean matches(FullHttpRequest httpRequest) {
                        return MOD_URI.equals(httpRequest.uri().split("/")[1]);
                    }
                },
                new MainHttpHandler(new Object())
        ));

        PlatformMain.INSTANCE.runOnServerStart(() -> updatePack(UpdateType.RESTART));
    }

    private static void addLogToAdminListeners() {
        LOGGER.addListener(MonkeyLibLogger.LogLevel.ERROR, createLogToAdminListener(MonkeyLibStyle.Color.RED));
        LOGGER.addListener(MonkeyLibLogger.LogLevel.WARN, createLogToAdminListener(MonkeyLibStyle.Color.YELLOW));
    }

    private static MonkeyLibLogger.LogListener createLogToAdminListener(final int textColor) {
        return (message, error) -> {
            final MonkeyLibText text = MonkeyLibText
                    .of("[%s] %s".formatted(MOD_ID, message))
                    .applyStyle(style -> style.withColor(textColor));

            if (error != null) text.applyStyle(style -> style.withShowText(MonkeyLibText.of(ExceptionUtils.getRootCauseMessage(error))));

            PlatformMain.INSTANCE.sendMessageToAdmins(text);
            MESSAGE_QUEUE.addLast(text);
        };
    }

    private static void createFolderStructure() throws GithubResourcepackManagerException {
        try {
            Files.createDirectories(RESOURCEPACK_OUTPUT_FOLDER);
            Files.createDirectories(DATAPACK_FOLDER);
            Files.createDirectories(GIT_FOLDER);
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to create directory!", e);
        }
    }

    public static void updatePack(final UpdateType updateType) {
        if (updateType != UpdateType.RESTART) {
            LOGGER.debug("Clearing admin message queue before updating after a restart...");
            MESSAGE_QUEUE.clear();
        }

        if (disabled) {
            LOGGER.warn("Skipping pack updating because config was invalid!");
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
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to create folder structure!", e);
        }

        // Git stuff
        final GitHandler gitHandler = new GitHandler();

        LOGGER.info("Updating git repository...");
        boolean failed = false;
        try {
            gitHandler.updateRepositoryAndGenerateCommitProperties();
        } catch (GithubResourcepackManagerException e) {
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
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to generate pack!", e);
            failed = updateFailed = true;
        }
        if (!failed) LOGGER.info("Pack location is '%s'!", resourcePackHandler.getOutputPackPath().toAbsolutePath());


        // Update server.properties file.
        try {
            PlatformServerProperties.INSTANCE.updatePackProperties(resourcePackHandler);
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to update server.properties file!", e);
        }

        // Generate placeholder map
        final Map<String, String> placeholders = generatePlaceholders(gitHandler, resourcePackHandler, updateType, "resource", wasUpdated);

        // Send chat message
        try {
            if (!failed) sendUpdateMessage(config.get().resourcePackProvider.updateMessage, wasUpdated, placeholders);
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to send update message for resource pack in chat!", e);
        }

        // Trigger webhooks
        if (!wasUpdated) {
            LOGGER.info("Not sending webhook because pack was not updated.");
            return;
        }
        if (!updateFailed) try {
            config.get().resourcePackProvider.successWebhook.trigger(true, placeholders, updateType);
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to trigger success webhook!", e);
        }
        else try {
            config.get().resourcePackProvider.failWebhook.trigger(false, placeholders, updateType);
        } catch (GithubResourcepackManagerException e) {
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
        } catch (GithubResourcepackManagerException e) {
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
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to send update message for datapack in chat!", e);
        }

        // Trigger webhooks
        if (!updateFailed) try {
            config.get().dataPackProvider.successWebhook.trigger(true, placeholders, updateType);
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to trigger success webhook!", e);
        }
        else try {
            config.get().dataPackProvider.failWebhook.trigger(false, placeholders, updateType);
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to trigger fail webhook!", e);
        }
    }

    private static void sendUpdateMessage(final String[] updateMessage, boolean wasUpdated, final Map<String, String> placeholders) throws GithubResourcepackManagerException {
        sendUpdateMessage(updateMessage, wasUpdated, placeholders, false);
    }

    private static void sendUpdateMessage(final String[] updateMessage, boolean wasUpdated, final Map<String, String> placeholders, boolean adminsOnly) throws GithubResourcepackManagerException {
        if (!wasUpdated) {
            LOGGER.info("Not sending chat message because pack was not updated.");
            return;
        }

        for (final MonkeyLibText line : createUpdateMessage(updateMessage, placeholders))
            PlatformText.INSTANCE.sendUpdateMessage(line, adminsOnly);
    }

    public static MonkeyLibText[] createUpdateMessage(final String[] updateMessage, final Map<String, String> placeholders) throws GithubResourcepackManagerException {
        final MonkeyLibText[] result = new MonkeyLibText[updateMessage.length];

        for (int lineIndex = 0; lineIndex < updateMessage.length; lineIndex++) {
            final String line = StringUtils.replacePlaceholders(updateMessage[lineIndex], placeholders, true, false);

            try {
                result[lineIndex] = TextFormattingApi.styleText(line);
            } catch (Exception e) {
                throw new GithubResourcepackManagerException("Failed to style update message at line %s!", e, lineIndex);
            }
        }

        return result;
    }

    public static Map<String, String> generatePlaceholders(final GitHandler gitHandler, final @Nullable ResourcePackHandler resourcePackHandler, final UpdateType updateType, final String packType, final boolean wasUpdated) {
        final Map<String, String> placeholders = new HashMap<>();

        if (gitHandler.getCommitProperties() != null) placeholders.putAll(gitHandler.getCommitProperties().toPlaceholdersMap());
        if (resourcePackHandler != null) placeholders.put("{downloadUrl}", config.get().getPackUrl(resourcePackHandler.getOutputPackName()));
        placeholders.putAll(STATIC_PLACEHOLDERS);
        placeholders.put("{packType}", packType);
        placeholders.put("{updateType}", updateType.name());
        placeholders.put("{wasUpdated}", String.valueOf(wasUpdated));
        LOGGER.info("Placeholders: %s", placeholders);

        return placeholders;
    }

    private static String getOldResourcePackName() {
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
