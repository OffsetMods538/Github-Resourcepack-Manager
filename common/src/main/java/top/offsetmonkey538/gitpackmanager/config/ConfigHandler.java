package top.offsetmonkey538.gitpackmanager.config;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import top.offsetmonkey538.gitpackmanager.config.webhook.BasicWebhook;
import top.offsetmonkey538.gitpackmanager.config.webhook.DefaultWebhookBody;
import top.offsetmonkey538.gitpackmanager.config.webhook.discord.basic.BasicFailMessage;
import top.offsetmonkey538.gitpackmanager.config.webhook.discord.basic.BasicSuccessMessage;
import top.offsetmonkey538.gitpackmanager.config.webhook.discord.embed.EmbedFailMessage;
import top.offsetmonkey538.gitpackmanager.config.webhook.discord.embed.EmbedSuccessMessage;
import top.offsetmonkey538.meshlib.common.api.MESHLibApi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static top.offsetmonkey538.gitpackmanager.GithubResourcepackManager.LOGGER;
import static top.offsetmonkey538.gitpackmanager.GithubResourcepackManager.config;
import static top.offsetmonkey538.offsetutils538.api.text.ArgReplacer.replaceArgs;

public final class ConfigHandler {
    private ConfigHandler() {

    }

    public static boolean handleConfig() {
        MESHLibApi.reload();

        LOGGER.info("Writing default webhook bodies");
        createDefaultWebhooks();

        // Checking if config is valid
        final List<String> errors = checkConfigErrors();

        boolean success = true;
        if (!errors.isEmpty()) {
            // There were errors, time to log em.
            LOGGER.error("There were problems with the config for Git Pack Manager, see below for more details!");
            errors.stream().map(string -> "    " + string).forEach(LOGGER::error);
            success = false;
        }
        if (MESHLibApi.getExternalPort() == null) {
            LOGGER.error("MESH Lib hasn't been configured correctly! Disabling Git Pack Manager!");
            success = false;
        }

        return !success;
    }

    private static void createDefaultWebhooks() {
        final Jankson jankson = new Jankson.Builder().build();
        final List<DefaultWebhookBody> webhookBodies = List.of(
                new BasicWebhook(),

                new BasicSuccessMessage(),
                new BasicFailMessage(),
                new EmbedSuccessMessage(),
                new EmbedFailMessage()
        );

        for (DefaultWebhookBody webhook : webhookBodies) {
            final Path location = config.get().getFilePath().getParent().resolve(webhook.getName());

            if (Files.exists(location)) continue;

            try {
                Files.createDirectories(location.getParent());
                Files.writeString(location, jankson.toJson(webhook).toJson(JsonGrammar.STRICT));
            } catch (IOException e) {
                LOGGER.error("Failed to write default webhook body '%s'!", webhook.getName(), e);
            }
        }
    }

    private static List<String> checkConfigErrors() {
        final List<String> errors = new ArrayList<>();

        if (config.get().serverInfo.publicIp == null) errors.add("Field 'serverInfo.publicIp' not set!");
        if (config.get().repositoryInfo.url == null) errors.add("Field 'repositoryInfo.url' not set!");
        if (config.get().repositoryInfo.isPrivate) {
            if (config.get().repositoryInfo.username == null) errors.add("Field 'repositoryInfo.username' not set, but 'repositoryInfo.isPrivate' is true!");
            if (config.get().repositoryInfo.token == null) errors.add("Field 'repositoryInfo.token' not set, but 'repositoryInfo.isPrivate' is true!");
        } else {
            if (config.get().repositoryInfo.username != null) errors.add("Field 'repositoryInfo.username' set, but 'repositoryInfo.isPrivate' is false! Can be unset");
            if (config.get().repositoryInfo.token != null) errors.add("Field 'repositoryInfo.token' set, but 'repositoryInfo.isPrivate' is false! Can be unset");
        }

        checkWebhookErrors(errors, config.get().resourcePackProvider.successWebhook, "resourcePackProvider.successWebhook");
        checkWebhookErrors(errors, config.get().resourcePackProvider.failWebhook, "resourcePackProvider.failWebhook");
        checkWebhookErrors(errors, config.get().dataPackProvider.successWebhook, "dataPackProvider.successWebhook");
        checkWebhookErrors(errors, config.get().dataPackProvider.failWebhook, "dataPackProvider.failWebhook");

        return errors;
    }

    private static void checkWebhookErrors(final List<String> errors, final ModConfig.WebhookInfo webhook, final String webhookPath) {
        if (!webhook.enabled) return;

        if (webhook.url == null) errors.add(replaceArgs("Field '%s.url' not set, but '%s.enabled' is true!", webhookPath, webhookPath));
        if (webhook.body == null) errors.add(replaceArgs("Field '%s.body' not set, but '%s.enabled' is true!", webhookPath, webhookPath));

        final Path bodyPath = webhook.getBodyPath();
        if (bodyPath != null && !Files.exists(bodyPath)) errors.add(replaceArgs("Field '%s.body' points to file '%s', which doesn't exist!", webhookPath, bodyPath.toAbsolutePath()));
    }
}
