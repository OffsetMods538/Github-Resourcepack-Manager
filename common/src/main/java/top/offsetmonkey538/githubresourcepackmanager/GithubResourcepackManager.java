package top.offsetmonkey538.githubresourcepackmanager;

import org.apache.commons.io.FileUtils;
import top.offsetmonkey538.githubresourcepackmanager.config.ModConfig;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.githubresourcepackmanager.handler.GitHandler;
import top.offsetmonkey538.githubresourcepackmanager.handler.PackHandler;
import top.offsetmonkey538.githubresourcepackmanager.networking.MainHttpHandler;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformCommand;
import top.offsetmonkey538.githubresourcepackmanager.config.ConfigManager;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformMain;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformServerProperties;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformText;
import top.offsetmonkey538.githubresourcepackmanager.utils.*;
import top.offsetmonkey538.meshlib.api.HttpHandlerRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

import static top.offsetmonkey538.githubresourcepackmanager.platform.PlatformLogging.LOGGER;

public final class GithubResourcepackManager {
    private GithubResourcepackManager() {

    }

    public static final String MOD_ID = "github-resourcepack-manager";
    public static final String MOD_URI = "gh-rp-manager";

    public static final Path RESOURCEPACK_FOLDER =  PlatformMain.INSTANCE.getConfigDir().resolve(".resource-pack");
    public static final Path REPO_ROOT_FOLDER = RESOURCEPACK_FOLDER.resolve("git");
    public static final Path OUTPUT_FOLDER = RESOURCEPACK_FOLDER.resolve("output");
    public static final Pattern PACK_NAME_PATTERN = Pattern.compile("\\d+-");
    public static final UUID PACK_UUID = UUID.fromString("60ab8dc7-08d1-4f5f-a9a8-9a01d048b7b9");


    public static ModConfig config;

    public static GitHandler gitHandler;
    public static PackHandler packHandler;


    public static void initialize() {
        PlatformCommand.INSTANCE.registerGithubRpManagerCommand();

        ConfigManager.loadConfig();

        try {
            createFolderStructure();
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to create folder structure!", e);
        }

        HttpHandlerRegistry.INSTANCE.register(MOD_URI, new MainHttpHandler());

        PlatformMain.INSTANCE.runOnServerStart(() -> updatePack(UpdateType.RESTART));
    }

    private static void createFolderStructure() throws GithubResourcepackManagerException {
        try {
            Files.createDirectories(OUTPUT_FOLDER);
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to create directory '%s'!", OUTPUT_FOLDER);
        }
    }

    public static void updatePack(final UpdateType updateType) {
        LOGGER.info("Updating resourcepack...");

        if (updateType == UpdateType.COMMAND_FORCE) {
            LOGGER.warn("Forced pack update! Deleting data directory and continuing...");
            try {
                FileUtils.deleteDirectory(RESOURCEPACK_FOLDER.toFile());
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
        gitHandler = new GitHandler();

        LOGGER.info("Updating git repository...");
        boolean failed = false;
        try {
            gitHandler.updateRepositoryAndGenerateCommitProperties();
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to update git repository!", e);
            failed = true;
        }
        if (!failed) LOGGER.info("Successfully updated git repository!");


        // Get the location of the old pack, if it exists.
        final String oldPackName = getOldPackName();
        final Path oldPackPath = oldPackName == null ? null : OUTPUT_FOLDER.resolve(oldPackName);


        // Check if pack was updated
        final boolean wasUpdated = gitHandler.getWasUpdated() || oldPackPath == null || !oldPackPath.toFile().exists();
        if (!wasUpdated) {
            LOGGER.info("Pack hasn't changed since last update. Skipping new pack generation.");
        }

        // Generate pack
        packHandler = new PackHandler();

        LOGGER.info("Getting pack location...");
        failed = false;
        try {
            packHandler.generatePack(wasUpdated, oldPackPath, oldPackName);
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to generate pack!", e);
            failed = true;
        }
        if (!failed) LOGGER.info("Pack location is '%s'!", packHandler.getOutputPackPath());


        // Update server.properties file.
        try {
            PlatformServerProperties.INSTANCE.updatePackProperties(packHandler);
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to update server.properties file!", e);
        }

        // Generate placeholder map
        final Map<String, String> placeholders = new HashMap<>();
        if (gitHandler.getCommitProperties() != null) placeholders.putAll(gitHandler.getCommitProperties().toPlaceholdersMap());
        placeholders.put("{downloadUrl}", config.getPackUrl(packHandler.getOutputPackName()));
        placeholders.put("{updateType}", updateType.name());
        placeholders.put("{wasUpdated}", String.valueOf(wasUpdated));
        LOGGER.info("Placeholders: %s", placeholders);

        // Send chat message
        try {
            sendUpdateMessage(wasUpdated, placeholders);
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to send update message in chat!", e);
        }

        // Trigger webhook
        try {
            triggerWebhook(wasUpdated, placeholders, updateType);
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to trigger webhook!", e);
        }


        LOGGER.info("Resourcepack updated!");
    }

    private static void triggerWebhook(boolean wasUpdated, Map<String, String> placeholders, UpdateType updateType) throws GithubResourcepackManagerException {
        // todo: change logic to match enable/disable and fail/success webhooks
        if (config.resourcePackProvider.successWebhook.url == null || config.resourcePackProvider.successWebhook.body == null) return;
        if (config.resourcePackProvider.successWebhook.body.contains("discord") && !wasUpdated) {
            LOGGER.info("Not sending discord webhook because pack was not updated.");
            return;
        }

        try {
            //noinspection DataFlowIssue: Only returns null when `config.webhookBody` is null, which we have already checked
            String webhookBody = Files.readString(config.getWebhookBody());
            webhookBody = StringUtils.replacePlaceholders(webhookBody, placeholders, true);

            WebhookSender.send(webhookBody, config.getWebhookUrl(), updateType, gitHandler.getWasUpdated());
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to read content of webhook body file '%s'!", e, config.resourcePackProvider.successWebhook.body);
        }
    }

    private static void sendUpdateMessage(boolean wasUpdated, final Map<String, String> placeholders) throws GithubResourcepackManagerException {
        if (!wasUpdated) {
            LOGGER.info("Not sending chat message because pack was not updated.");
            return;
        }

        PlatformText.INSTANCE.sendUpdateMessage(placeholders);
    }

    private static String getOldPackName() {
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
