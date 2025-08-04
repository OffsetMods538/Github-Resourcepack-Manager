package top.offsetmonkey538.githubresourcepackmanager.config;

import blue.endless.jankson.*;
import org.jetbrains.annotations.NotNull;
import top.offsetmonkey538.githubresourcepackmanager.config.webhook.BasicWebhook;
import top.offsetmonkey538.githubresourcepackmanager.config.webhook.DefaultWebhookBody;
import top.offsetmonkey538.githubresourcepackmanager.config.webhook.discord.basic.BasicFailMessage;
import top.offsetmonkey538.githubresourcepackmanager.config.webhook.discord.basic.BasicSuccessMessage;
import top.offsetmonkey538.githubresourcepackmanager.config.webhook.discord.embed.EmbedFailMessage;
import top.offsetmonkey538.githubresourcepackmanager.config.webhook.discord.embed.EmbedSuccessMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.*;
import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.LOGGER;

public final class ConfigHandler {
    private ConfigHandler() {

    }

    public static boolean handleConfig() {
        LOGGER.info("Writing default webhook bodies");
        createDefaultWebhooks();

        // Checking if config is valid
        final List<String> errors = checkConfigErrors();

        // Return false when nothing's wrong (disables the mod)
        if (errors.isEmpty()) return false;

        // There were errors, time to log em.
        LOGGER.error("There were problems with the config for GitHub Resourcepack Manager, see below for more details!");
        errors.stream().map(string -> "    " + string).forEach(LOGGER::error);
        return true;
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

    private static @NotNull List<String> checkConfigErrors() {
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

        if (webhook.url == null) errors.add("Field '%s.url' not set, but '%s.enabled' is true!".formatted(webhookPath, webhookPath));
        if (webhook.body == null) errors.add("Field '%s.body' not set, but '%s.enabled' is true!".formatted(webhookPath, webhookPath));

        final Path bodyPath = webhook.getBodyPath();
        if (bodyPath != null && !Files.exists(bodyPath)) errors.add("Field '%s.body' points to file '%s', which doesn't exist!".formatted(webhookPath, bodyPath.toAbsolutePath()));
    }
}
