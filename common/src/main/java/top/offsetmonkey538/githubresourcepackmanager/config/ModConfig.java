package top.offsetmonkey538.githubresourcepackmanager.config;

import blue.endless.jankson.Comment;
import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import top.offsetmonkey538.githubresourcepackmanager.config.webhook.BasicWebhook;
import top.offsetmonkey538.githubresourcepackmanager.config.webhook.DefaultWebhookBody;
import top.offsetmonkey538.githubresourcepackmanager.config.webhook.discord.BasicMessage;
import top.offsetmonkey538.githubresourcepackmanager.config.webhook.discord.EmbedMessage;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformServerProperties;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.*;
import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.config;
import static top.offsetmonkey538.githubresourcepackmanager.config.ConfigManager.CURRENT_CONFIG_FILE_PATH;
import static top.offsetmonkey538.githubresourcepackmanager.platform.PlatformLogging.LOGGER;

public class ModConfig {
    @Comment("!!!!Please check the wiki for how to set up the mod. It is linked on both the Modrinth and GitHub pages!!!!")
    public String packUpdateMessage = "Server resourcepack has been updated!\nPlease click {packUpdateCommand} to get the most up to date pack.";
    public String packUpdateMessageHoverMessage = "{longDescription}";
    @Comment("The public ip of your server (123.45.67.89 or play.coolserver.net)")
    public String serverPublicIp = null;
    @Comment("Should be \"[YOUR BRANCH NAME HERE]\"")
    public String branch = "master";
    public String repoUrl = null;
    @Comment("Where the mod will search for resource packs in the cloned repository")
    public String resourcePackRoot = "";
    public boolean isRepoPrivate = false;
    public String githubUsername = null;
    @Comment("PLEASE DON'T SHARE THIS WITH ANYONE EVER")
    public String githubToken = null;
    public String webhookUrl = null;
    public String webhookBody = null;


    protected String getName() {
        return MOD_ID + "/" + MOD_ID;
    }

    protected int getConfigVersion() {
        return 1;
    }

    protected List<ConfigManager.Datafixer> getDatafixers() {
        return List.of(
                (original, jankson) -> {
                    // 0 -> 1
                    original.put("branch", jankson.toJson(jankson.getMarshaller().marshall(String.class, original.get("githubRef")).replace("refs/heads/", "")));
                    original.put("repoUrl", original.get("githubUrl"));
                    original.put("isRepoPrivate", original.get("isPrivate"));
                }
        );
    }

    public void createDefaultWebhooks() {
        final Jankson jankson = new Jankson.Builder().build();
        final List<DefaultWebhookBody> webhookBodies = List.of(new BasicWebhook(), new BasicMessage(), new EmbedMessage());

        for (DefaultWebhookBody webhook : webhookBodies) {
            final Path location = CURRENT_CONFIG_FILE_PATH.getParent().resolve(webhook.getName());

            if (Files.exists(location)) continue;

            try {
                Files.createDirectories(location.getParent());
                Files.writeString(location, jankson.toJson(webhook).toJson(JsonGrammar.STRICT));
            } catch (IOException e) {
                LOGGER.error("Failed to write default webhook body '%s'!", webhook.getName(), e);
            }
        }
    }

    public String getPackUrl(String outputFileName) {
        return String.format(
                "http://%s:%s/%s",
                serverPublicIp,
                PlatformServerProperties.INSTANCE.getServerPort(),
                outputFileName
        );
    }

    public URI getWebhookUrl() {
        if (webhookUrl == null) return null;
        return URI.create(webhookUrl);
    }

    public Path getWebhookBody() {
        if (webhookBody == null) return null;
        return CURRENT_CONFIG_FILE_PATH.getParent().resolve(webhookBody);
    }

    public Path getResourcePackRoot() {
        return REPO_ROOT_FOLDER.resolve(config.resourcePackRoot.startsWith("/") ? config.resourcePackRoot.substring(1) : config.resourcePackRoot);
    }
    public Path getPacksDir() {
        return getResourcePackRoot().resolve("packs");
    }

    public String getGithubRef() {
        return "refs/heads/" + branch;
    }
}
