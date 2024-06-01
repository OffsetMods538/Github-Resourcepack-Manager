package top.offsetmonkey538.githubresourcepackmanager;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.offsetmonkey538.githubresourcepackmanager.config.ModConfig;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.githubresourcepackmanager.handler.GitHandler;
import top.offsetmonkey538.githubresourcepackmanager.handler.PackHandler;
import top.offsetmonkey538.githubresourcepackmanager.handler.WebserverHandler;
import top.offsetmonkey538.githubresourcepackmanager.utils.*;
import top.offsetmonkey538.monkeylib538.config.ConfigManager;
import top.offsetmonkey538.monkeylib538.text.TextUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public class GithubResourcepackManager implements DedicatedServerModInitializer {
    public static final String MOD_ID = "github-resourcepack-manager";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Path OLD_CONFIG_FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");
    public static final Path NEW_CONFIG_FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID).resolve(MOD_ID + ".json");
    public static final Path RESOURCEPACK_FOLDER = FabricLoader.getInstance().getGameDir().resolve("resourcepack");
    public static final Path REPO_ROOT_FOLDER = RESOURCEPACK_FOLDER.resolve("git");
    public static final Path PACKS_FOLDER = REPO_ROOT_FOLDER.resolve("packs");
    public static final Path OUTPUT_FOLDER = RESOURCEPACK_FOLDER.resolve("output");
    public static final Pattern PACK_NAME_PATTERN = Pattern.compile("\\d+-");


    public static ModConfig config;

    public static MinecraftDedicatedServer minecraftServer;
    public static WebserverHandler webserverHandler;
    public static GitHandler gitHandler;
    public static PackHandler packHandler;


    @Override
    public void onInitializeServer() {
        loadConfig();

        try {
            createFolderStructure();
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to create folder structure!", e);
        }

        webserverHandler = new WebserverHandler();
        webserverHandler.initialize();

        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> {
            GithubResourcepackManager.minecraftServer = (MinecraftDedicatedServer) minecraftServer;

            updatePack(false);
        });
    }

    private static void createFolderStructure() throws GithubResourcepackManagerException {
        try {
            Files.createDirectories(OUTPUT_FOLDER);
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to create directory '%s'!", OUTPUT_FOLDER);
        }
    }

    private static void loadConfig() {
        if (Files.exists(OLD_CONFIG_FILE_PATH)) {
            try {
                Files.createDirectories(NEW_CONFIG_FILE_PATH.getParent());
                Files.move(OLD_CONFIG_FILE_PATH, NEW_CONFIG_FILE_PATH);
            } catch (IOException e) {
                throw new RuntimeException("Failed to move config file to new location!", e);
            }
        }

        config = ConfigManager.init(new ModConfig(), LOGGER::error);

        if (config.serverPublicIp == null || config.githubUrl == null || (config.isPrivate && (config.githubUsername == null || config.githubToken == null))) {
            LOGGER.error("Please fill in the config file!");
            throw new RuntimeException("Please fill in the config file!");
        }
    }

    public static void updatePack(boolean isWebhook) {
        LOGGER.info("Updating resourcepack...");

        final WebhookSender.UpdateType updateType = isWebhook ? WebhookSender.UpdateType.RUNTIME : WebhookSender.UpdateType.RESTART;

        // Git stuff
        gitHandler = new GitHandler();

        try {
            gitHandler.updateRepositoryAndGenerateCommitProperties();
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to update git repository!", e);
        }


        // Get the location of the old pack, if it exists.
        final String oldPackName = getOldPackName();
        final Path oldPackPath = oldPackName == null ? null : OUTPUT_FOLDER.resolve(oldPackName);


        // Generate pack
        final boolean wasUpdated = gitHandler.getWasUpdated() || oldPackPath == null || !oldPackPath.toFile().exists();
        packHandler = new PackHandler();

        try {
            packHandler.generatePack(wasUpdated, oldPackPath, oldPackName);
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to generate pack!", e);
        }


        // Update server.properties file if pack was updated.
        try {
            if (wasUpdated) ServerPropertiesUtils.updatePackProperties(minecraftServer, packHandler);
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to update server.properties file!", e);
        }

        // Generate placeholder map
        final Map<String, String> placeholders = new HashMap<>();
        if (gitHandler.getCommitProperties() != null) placeholders.putAll(gitHandler.getCommitProperties().toPlaceholdersMap());
        placeholders.put("{downloadUrl}", config.getPackUrl(packHandler.getOutputPackName()));
        placeholders.put("{updateType}", updateType.name());
        placeholders.put("{wasUpdated}", String.valueOf(wasUpdated));

        // Send chat message
        try {
            sendUpdateMessage(placeholders);
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

    private static void triggerWebhook(boolean wasUpdated, Map<String, String> placeholders, WebhookSender.UpdateType updateType) throws GithubResourcepackManagerException {
        if (config.webhookUrl == null || config.webhookBody == null) return;
        if (config.webhookBody.toString().contains("discord") && !wasUpdated) {
            LOGGER.info("Not sending discord webhook because pack was not updated.");
            return;
        }

        try {
            String webhookBody = Files.readString(config.webhookBody);
            webhookBody = StringUtils.replacePlaceholders(webhookBody, placeholders);

            WebhookSender.send(webhookBody, config.webhookUrl, updateType, gitHandler.getWasUpdated());
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to read content of webhook body file '%s'!", e, config.webhookBody);
        }
    }

    private static void sendUpdateMessage(final Map<String, String> placeholders) throws GithubResourcepackManagerException {
        String message = config.packUpdateMessage;
        message = StringUtils.replacePlaceholders(message, placeholders);

        final String[] splitMessage = message.split("\n");

        for (int lineNumber = 0; lineNumber < splitMessage.length; lineNumber++) {
            final String currentLineString = splitMessage[lineNumber];
            final Text currentLine;
            try {
                currentLine = TextUtils.INSTANCE.getStyledText(currentLineString);
            } catch (Exception e) {
                throw new GithubResourcepackManagerException("Failed to style update message at line number '%s'!", e, lineNumber);
            }


            final PlayerManager playerManager = minecraftServer.getPlayerManager();

            if (playerManager != null) playerManager.broadcast(currentLine, false);
        }
    }

    private static String getOldPackName() {
        final String oldPackUrl = ServerPropertiesUtils.getResourcePackUrl(minecraftServer);
        if (oldPackUrl == null) return null;

        int nameStartIndex = oldPackUrl.lastIndexOf('/');
        if (nameStartIndex == -1) return null;

        return oldPackUrl.substring(nameStartIndex + 1);
    }
}
