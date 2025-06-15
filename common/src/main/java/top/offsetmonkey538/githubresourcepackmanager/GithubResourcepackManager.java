package top.offsetmonkey538.githubresourcepackmanager;

import org.apache.commons.io.FileUtils;
import top.offsetmonkey538.githubresourcepackmanager.config.ModConfig;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.githubresourcepackmanager.handler.GitHandler;
import top.offsetmonkey538.githubresourcepackmanager.handler.ResourcePackHandler;
import top.offsetmonkey538.githubresourcepackmanager.networking.MainHttpHandler;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformCommand;
import top.offsetmonkey538.githubresourcepackmanager.config.ConfigManager;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformMain;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformServerProperties;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformText;
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

    public static final Path DATA_FOLDER =  PlatformMain.INSTANCE.getConfigDir().resolve(".packs");
    public static final Path GIT_FOLDER = DATA_FOLDER.resolve("git");

    public static final Path RESOURCEPACK_FOLDER =  DATA_FOLDER.resolve("resource-pack");
    public static final Path DATAPACK_FOLDER =  DATA_FOLDER.resolve("data-pack");

    public static final Path RESOURCEPACK_OUTPUT_FOLDER = RESOURCEPACK_FOLDER.resolve("output");

    public static final Pattern RESOURCEPACK_NAME_PATTERN = Pattern.compile("\\d+-");
    public static final UUID RESOURCEPACK_UUID = UUID.fromString("60ab8dc7-08d1-4f5f-a9a8-9a01d048b7b9");

    public static ModConfig config;

     public static ResourcePackHandler resourcePackHandler;

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
            Files.createDirectories(RESOURCEPACK_OUTPUT_FOLDER);
            Files.createDirectories(DATAPACK_FOLDER);
            Files.createDirectories(GIT_FOLDER);
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to create directory '%s'!", RESOURCEPACK_OUTPUT_FOLDER);
        }
    }

    public static void updatePack(final UpdateType updateType) {
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

        if (config.resourcePackProvider.enabled) {
            LOGGER.info("");
            LOGGER.info("Updating resource pack...");
            updateResourcePack(gitHandler, updateType, failed);
            LOGGER.info("Resource pack updated!");
        }
        if (config.dataPackProvider.enabled) {
            LOGGER.info("");
            LOGGER.info("Updating data pack...");
            // TODO: implement datapack stuff
            LOGGER.info("Data pack updated!");
        }
    }

    private static void updateResourcePack(final GitHandler gitHandler, final UpdateType updateType, boolean updateFailed) {
        // Get the location of the old pack, if it exists.
        final String oldResourcePackName = getOldResourcePackName();
        final Path oldResourcePackPath = oldResourcePackName == null ? null : RESOURCEPACK_OUTPUT_FOLDER.resolve(oldResourcePackName);

        // Check if pack was updated
        final boolean wasUpdated =
                gitHandler.getChangedFiles().map(changes -> changes.stream().anyMatch(it -> it.startsWith(config.resourcePackProvider.getRootLocation()))).orElse(true)
                        || oldResourcePackPath == null
                        || !oldResourcePackPath.toFile().exists();
        if (!wasUpdated) {
            LOGGER.info("Pack hasn't changed since last update. Skipping new pack generation.");
        }

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
        final Map<String, String> placeholders = new HashMap<>();
        if (gitHandler.getCommitProperties() != null) placeholders.putAll(gitHandler.getCommitProperties().toPlaceholdersMap());
        placeholders.put("{downloadUrl}", config.getPackUrl(resourcePackHandler.getOutputPackName()));
        placeholders.put("{updateType}", updateType.name());
        placeholders.put("{wasUpdated}", String.valueOf(wasUpdated));
        LOGGER.info("Placeholders: %s", placeholders);

        // Send chat message
        try {
            sendUpdateMessage(wasUpdated, placeholders);
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to send update message in chat!", e);
        }

        // Trigger webhooks
        if (!wasUpdated) {
            LOGGER.info("Not sending webhook because pack was not updated.");
            return;
        }
        if (!updateFailed) try {
            config.resourcePackProvider.successWebhook.trigger(true, placeholders, updateType);
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to trigger success webhook!", e);
        }
        else try {
            config.resourcePackProvider.failWebhook.trigger(false, placeholders, updateType);
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to trigger fail webhook!", e);
        }
    }

    private static void sendUpdateMessage(boolean wasUpdated, final Map<String, String> placeholders) throws GithubResourcepackManagerException {
        if (!wasUpdated) {
            LOGGER.info("Not sending chat message because pack was not updated.");
            return;
        }

        PlatformText.INSTANCE.sendUpdateMessage(placeholders);
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
