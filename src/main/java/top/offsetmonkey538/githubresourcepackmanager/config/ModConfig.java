package top.offsetmonkey538.githubresourcepackmanager.config;

import blue.endless.jankson.Comment;
import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import top.offsetmonkey538.githubresourcepackmanager.config.webhook.BasicWebhook;
import top.offsetmonkey538.githubresourcepackmanager.config.webhook.DefaultWebhookBody;
import top.offsetmonkey538.githubresourcepackmanager.config.webhook.discord.BasicMessage;
import top.offsetmonkey538.githubresourcepackmanager.config.webhook.discord.EmbedMessage;
import top.offsetmonkey538.monkeylib538.config.Config;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.*;
import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.config;

public class ModConfig extends Config {
    @Comment("!!!!Please check the wiki for how to set up the mod. It is linked on both the Modrinth and GitHub pages!!!!")
    public String packUpdateMessage = "Server resourcepack has been updated!\nPlease click {packUpdateCommand} to get the most up to date pack.";
    public String packUpdateMessageHoverMessage = "{longDescription}";
    @Comment("The public ip of your server (123.45.67.89 or play.coolserver.net)")
    public String serverPublicIp = null;
    @Comment("Should be \"refs/heads/[YOUR BRANCH NAME HERE]\"")
    public String githubRef = "refs/heads/master";
    public String githubUrl = null;
    public boolean isPrivate = false;
    public String githubUsername = null;
    @Comment("PLEASE DON'T SHARE THIS WITH ANYONE EVER")
    public String githubToken = null;
    public String webhookUrl = null;
    public String webhookBody = null;
    @Comment("Where the mod will search for resource packs in the cloned repository")
    public String resourcePackRoot = "";

    @Override
    protected String getName() {
        return MOD_ID + "/" + MOD_ID;
    }

    public void createDefaultWebhooks() {
        final Jankson jankson = new Jankson.Builder().build();
        final List<DefaultWebhookBody> webhookBodies = List.of(new BasicWebhook(), new BasicMessage(), new EmbedMessage());

        for (DefaultWebhookBody webhook : webhookBodies) {
            final Path location = getFilePath().getParent().resolve(webhook.getName());

            if (Files.exists(location)) continue;

            try {
                Files.createDirectories(location.getParent());
                Files.writeString(location, jankson.toJson(webhook).toJson(JsonGrammar.STRICT));
            } catch (IOException e) {
                LOGGER.error("Failed to write default webhook body '{}'!", webhook.getName(), e);
            }
        }
    }

    public String getPackUrl(String outputFileName, MinecraftServer server) {
        return String.format(
                "http://%s:%s/%s",
                serverPublicIp,
                server.getServerPort(),
                outputFileName
        );
    }

    @Nullable
    public URI getWebhookUrl() {
        if (webhookUrl == null) return null;
        return URI.create(webhookUrl);
    }

    @Nullable
    public Path getWebhookBody() {
        if (webhookBody == null) return null;
        return getFilePath().getParent().resolve(webhookBody);
    }

    public Path getResourcePackRoot() {
        return REPO_ROOT_FOLDER.resolve(config.resourcePackRoot.startsWith("/") ? config.resourcePackRoot.substring(1) : config.resourcePackRoot);
    }
    public Path getPacksDir() {
        return getResourcePackRoot().resolve("packs");
    }
}
