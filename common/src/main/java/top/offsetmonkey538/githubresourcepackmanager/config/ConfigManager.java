package top.offsetmonkey538.githubresourcepackmanager.config;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.SyntaxError;
import org.jetbrains.annotations.NotNull;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformMain;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.*;
import static top.offsetmonkey538.githubresourcepackmanager.platform.PlatformLogging.LOGGER;

public final class ConfigManager {
    private ConfigManager() {

    }

    public static final String VERSION_KEY = "!!!version";

    public static final Path OLD_CONFIG_FILE_PATH = PlatformMain.INSTANCE.getConfigDir().getParent().resolve(MOD_ID + ".json");
    public static final Path NEW_CONFIG_FILE_PATH = PlatformMain.INSTANCE.getConfigDir().resolve(MOD_ID + ".json");
    public static final Path CURRENT_CONFIG_FILE_PATH = PlatformMain.INSTANCE.getConfigDir().resolve("config").resolve("main.json");


    public static void loadConfig() {
        if (Files.exists(OLD_CONFIG_FILE_PATH)) {
            try {
                Files.createDirectories(NEW_CONFIG_FILE_PATH.getParent());
                Files.move(OLD_CONFIG_FILE_PATH, NEW_CONFIG_FILE_PATH);
            } catch (IOException e) {
                throw new RuntimeException("Failed to move config file to new location!", e);
            }
        }
        if (Files.exists(NEW_CONFIG_FILE_PATH)) {
            try {
                Files.createDirectories(CURRENT_CONFIG_FILE_PATH.getParent());
                Files.move(NEW_CONFIG_FILE_PATH, CURRENT_CONFIG_FILE_PATH);
            } catch (IOException e) {
                throw new RuntimeException("Failed to move config file to new location!", e);
            }
        }

        config = new ModConfig();
        if (CURRENT_CONFIG_FILE_PATH.toFile().exists()) config = load();
        save(config);

        LOGGER.info("Writing default webhook bodies");
        config.createDefaultWebhooks();

        // Checking if config is valid
        final List<String> errors = checkConfigErrors();

        // Return when nothing's wrong
        if (errors.isEmpty()) return;

        // There were errors, time to log em.
        LOGGER.error("There were problems with the config for GitHub Resourcepack Manager, see below for more details!");
        errors.stream().map(string -> "\t" + string).forEach(LOGGER::error);
        LOGGER.error("There were problems with the config for Github Resourcepack Manager, see above for more details!");

        throw new RuntimeException("There were problems with the config for Github Resourcepack Manager, see above for more details!");
    }

    private static @NotNull List<String> checkConfigErrors() {
        final List<String> errors = new ArrayList<>();

        if (config.serverPublicIp == null) errors.add("Field 'serverPublicIp' not set!");
        if (config.repoUrl == null) errors.add("Field 'repoUrl' not set!");
        if (config.isRepoPrivate) {
            if (config.githubUsername == null) errors.add("Field 'githubUsername' not set, but 'isRepoPrivate' is true!");
            if (config.githubToken == null) errors.add("Field 'githubToken' not set, but 'isRepoPrivate' is true!");
        } else {
            if (config.githubUsername != null) errors.add("Field 'githubUsername' set, but 'isRepoPrivate' is false! Can be unset");
            if (config.githubToken != null) errors.add("Field 'githubToken' set, but 'isRepoPrivate' is false! Can be unset");
        }
        return errors;
    }

    private static ModConfig load() {
        final Jankson jankson = Jankson.builder().build();
        final File configFile = CURRENT_CONFIG_FILE_PATH.toFile();

        // Load the config from disk
        final JsonObject json;
        try {
            json = jankson.load(configFile);
        } catch (IOException e) {
            LOGGER.error("Config file '%s' could not be read!", CURRENT_CONFIG_FILE_PATH, e);
            return config;
        } catch (SyntaxError e) {
            LOGGER.error("Config file '%s' is formatted incorrectly!", CURRENT_CONFIG_FILE_PATH, e);
            return config;
        }

        // Check if version matches the latest version
        applyDatafixers(config, configFile.toPath(), json, jankson);

        // Load config class from the final json
        return jankson.fromJson(json, config.getClass());
    }

    private static void applyDatafixers(ModConfig config, Path configFile, JsonObject json, Jankson jankson) {
        int last_version = json.getInt(VERSION_KEY, 0);
        int current_version = config.getConfigVersion();
        if (last_version < current_version) try {
            final Path backupPath = configFile.resolveSibling(String.format("%s-%s-backup.json", configFile.getFileName(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss"))));
            Files.copy(configFile, backupPath);

            for (Datafixer datafixer : config.getDatafixers().subList(last_version, current_version)) {
                datafixer.apply(json, jankson);
            }
            save(config);
        } catch (IOException e) {
            LOGGER.error("Unable to create backup of config file '%s'! Continuing anyway cause I don't care if your config gets messed up and I can't think of a reason for this even happening cause like the initial config file has to be there so I'd imagine that the directory is writeable so like why wouldn't it be possible to write the backup of the file?", configFile, e);
        }

        if (last_version > current_version) {
            throw new IllegalStateException(String.format("Config file '%s' is for a newer version of the mod, please update! Expected config version '%s', found '%s'!", CURRENT_CONFIG_FILE_PATH, current_version, last_version));
        }
    }

    private static void save(ModConfig config) {
        final Jankson jankson = Jankson.builder().build();

        // Convert config to json
        final JsonElement jsonAsElement = jankson.toJson(config);
        if (!(jsonAsElement instanceof final JsonObject json)) {
            LOGGER.error("Could not cast '%s' to 'JsonObject'! Config will not be saved!", jsonAsElement.getClass().getName());
            return;
        }

        // Write config version
        json.put(VERSION_KEY, new JsonPrimitive(config.getConfigVersion()), "!!!!! DO NOT MODIFY THIS VALUE !!!!");

        // Convert final json to string
        final String result = json.toJson(true, true);

        try {
            Files.createDirectories(CURRENT_CONFIG_FILE_PATH.getParent());
            Files.writeString(CURRENT_CONFIG_FILE_PATH, result);
        } catch (IOException e) {
            LOGGER.error("Config file '%s' could not be written to!", CURRENT_CONFIG_FILE_PATH, e);
        }
    }

    @FunctionalInterface
    public interface Datafixer {
        void apply(JsonObject original, Jankson jankson);
    }
}
