package top.offsetmonkey538.githubresourcepackmanager.config;

import blue.endless.jankson.Comment;
import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.api.Marshaller;
import org.jetbrains.annotations.Nullable;
import top.offsetmonkey538.githubresourcepackmanager.config.webhook.BasicWebhook;
import top.offsetmonkey538.githubresourcepackmanager.config.webhook.DefaultWebhookBody;
import top.offsetmonkey538.githubresourcepackmanager.config.webhook.discord.basic.BasicFailMessage;
import top.offsetmonkey538.githubresourcepackmanager.config.webhook.discord.basic.BasicSuccessMessage;
import top.offsetmonkey538.githubresourcepackmanager.config.webhook.discord.embed.EmbedFailMessage;
import top.offsetmonkey538.githubresourcepackmanager.config.webhook.discord.embed.EmbedSuccessMessage;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformServerProperties;
import top.offsetmonkey538.githubresourcepackmanager.utils.StringUtils;
import top.offsetmonkey538.githubresourcepackmanager.utils.WebhookSender;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.*;
import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.config;
import static top.offsetmonkey538.githubresourcepackmanager.config.ConfigManager.CURRENT_CONFIG_FILE_PATH;
import static top.offsetmonkey538.githubresourcepackmanager.platform.PlatformLogging.LOGGER;

public class ModConfig {

    @Comment("!!!!Please check the wiki for how to set up the mod. It is linked on both the Modrinth and GitHub pages!!!!")
    public ServerInfo serverInfo = new ServerInfo();
    public RepositoryInfo repositoryInfo = new RepositoryInfo();
    public ResourcePackProvider resourcePackProvider = new ResourcePackProvider();
    public DataPackProvider dataPackProvider = new DataPackProvider();


    public static class ServerInfo {
        @Comment("The public ip of your server (\"123.45.67.89\" or \"play.coolserver.net\")")
        public String publicIp = null;

        @Comment("If set, this port will be used in the server.properties file instead of the Minecraft server port. HTTP server will still be hosted on the Minecraft port. Only useful when running the server behind a proxy like nginx, traefik, cloudflare tunnel, etc.")
        public String proxyPort = null;
    }

    public static class RepositoryInfo {
        @Comment("Should be \"[YOUR BRANCH NAME HERE]\". Common names include \"master\" and \"main\"")
        public String branch = "master";
        @Comment("The URL of your repository. For example \"https://github.com/MyName/MyRepository\"")
        public String url = null;

        @Comment("Whether or not the repository is private. Username and token will need to be populated when this is set to 'true'!")
        public boolean isPrivate = false;

        @Comment("The two values below only need to be set when 'isPrivate' is true!")
        public String username = null;
        @Comment("PLEASE DO NOT SHARE THIS WITH ANYONE")
        public String token = null;
    }

    public static class ResourcePackProvider {
        @Comment("Whether or not the resource pack provider is enabled. Default: true")
        public boolean enabled = true;
        @Comment("Where the mod will search for resource packs in the cloned repository. MUST NOT be same as or child of the 'rootLocation' of the datapack provider")
        public String rootLocation = "/resourcepacks";

        @Comment("Message sent in chat when pack has been updated. May be 'null' to disable.")
        public String updateMessage = "Server resourcepack has been updated!\nPlease click {packUpdateCommand} to get the most up to date pack.";
        @Comment("Message shown when hovering over the 'updateMessage' text. May be 'null' to disable.")
        public String updateMessageHoverMessage = "{longDescription}";

        @Comment("Webhook to be sent when pack updating succeeded")
        public WebhookInfo successWebhook = new WebhookInfo();
        @Comment("Webhook to be sent when pack updating failed")
        public WebhookInfo failWebhook = new WebhookInfo();

        public String getRootLocation() {
            return rootLocation.startsWith("/") ? rootLocation.substring(1) : rootLocation;
        }
    }

    public static class DataPackProvider {
        @Comment("Whether or not the data pack provider is enabled. Default: false")
        public boolean enabled = false;
        @Comment("Where the mod will search for data packs in the cloned repository. MUST NOT be same as or child of the 'rootLocation' of the resourcepack provider")
        public String rootLocation = "/datapacks";

        @Comment("Message sent TO ADMINS in chat when pack has been updated. May be 'null' to disable.")
        public String updateMessage = "Server datapacks has been updated!\nNew packs (if any) have been enabled automatically.\nPlease run '/reload' or restart the server to reload datapacks.";
        @Comment("Message shown when hovering over the 'updateMessage' text. May be 'null' to disable.")
        public String updateMessageHoverMessage = "{longDescription}";

        @Comment("Webhook to be sent when pack updating succeeded")
        public WebhookInfo successWebhook = new WebhookInfo();
        @Comment("Webhook to be sent when pack updating failed")
        public WebhookInfo failWebhook = new WebhookInfo();

        public String getRootLocation() {
            return rootLocation.startsWith("/") ? rootLocation.substring(1) : rootLocation;
        }
    }

    public static class WebhookInfo {
        public WebhookInfo() {

        }
        public WebhookInfo(boolean enabled, String url, String body) {
            this.enabled = enabled;
            this.url = url;
            this.body = body;
        }

        @Comment("Whether or not this webhook is enabled")
        public boolean enabled = false;

        @Comment("The URL to send the webhook to. For example \"https://discord.com/api/webhooks/1234567890123456789/eW91J3JlIG5vdCBzdGVhbGluZyBhIHRva2Vu_bm9wZQ==_eWVyJyBub3Q=\" or something custom like \"https://api.example.com/NDI6IHRoZSBtZWFuaW5nIG9mIGxpZmUsIHRoZSB1bml2ZXJzZSwgYW5kIGV2ZXJ5dGhpbmc=\"")
        public String url = null;
        @Comment("The relative path from the config directory to a webhook body file. For example \"discord/basic_message.json\" or \"discord/embed_message.json\"")
        public String body = null;

        public void trigger(final boolean updateSucceeded, final Map<String, String> placeholders, final UpdateType updateType) throws GithubResourcepackManagerException {
            if (!enabled) return;

            try {
                //noinspection DataFlowIssue: Only returns null when `body` is null, which we have already checked
                String webhookBody = Files.readString(getBodyPath());
                webhookBody = StringUtils.replacePlaceholders(webhookBody, placeholders, true);

                WebhookSender.send(webhookBody, config.getWebhookUrl(), updateType, updateSucceeded);
            } catch (IOException e) {
                throw new GithubResourcepackManagerException("Failed to read content of webhook body file '%s'!", e, config.resourcePackProvider.successWebhook.body);
            }
        }

        public @Nullable Path getBodyPath() {
            return body == null ? null : CURRENT_CONFIG_FILE_PATH.getParent().resolve(body);
        }
    }

    //@Comment("!!!!Please check the wiki for how to set up the mod. It is linked on both the Modrinth and GitHub pages!!!!")
    //public String packUpdateMessage = "Server resourcepack has been updated!\nPlease click {packUpdateCommand} to get the most up to date pack.";
    //public String packUpdateMessageHoverMessage = "{longDescription}";
    //@Comment("The public ip of your server (\"123.45.67.89\" or \"play.coolserver.net\")")
    //public String serverPublicIp = null;
    //@Comment("If set, this port will be used in the server.properties file instead of the Minecraft server port. HTTP server will still be hosted on the Minecraft port. Only useful when running the server behind a proxy like nginx, traefik, cloudflare tunnel, etc.")
    //public String proxyPort = null;
    //@Comment("Should be \"[YOUR BRANCH NAME HERE]\". Common names include \"master\" and \"main\"")
    //public String branch = "master";
    //public String repoUrl = null;
    //@Comment("Where the mod will search for resource packs in the cloned repository")
    //public String resourcePackRoot = "";
    //public boolean isRepoPrivate = false;
    //public String githubUsername = null;
    //@Comment("PLEASE DON'T SHARE THIS WITH ANYONE EVER")
    //public String githubToken = null;
    //public String webhookUrl = null;
    //public String webhookBody = null;


    protected String getName() {
        return MOD_ID + "/" + MOD_ID;
    }

    protected int getConfigVersion() {
        return 3;
    }

    protected List<ConfigManager.Datafixer> getDatafixers() {
        return List.of(
                (original, jankson) -> {
                    // 0 -> 1
                    original.put("branch", jankson.toJson(jankson.getMarshaller().marshall(String.class, original.get("githubRef")).replace("refs/heads/", "")));
                    original.put("repoUrl", original.get("githubUrl"));
                    original.put("isRepoPrivate", original.get("isPrivate"));
                },
                (original, jankson) -> {
                    // 1 -> 2
                    // noop
                },
                (original, jankson) -> {
                    // 2 -> 3

                    final Marshaller marsh = jankson.getMarshaller();

                    // Server Info
                    final ServerInfo serverInfo = new ServerInfo();

                    serverInfo.publicIp = marsh.marshall(String.class, original.get("serverPublicIp"));
                    serverInfo.proxyPort = marsh.marshall(String.class, original.get("proxyPort"));

                    original.put("serverInfo", jankson.toJson(serverInfo));

                    // Repository Info
                    final RepositoryInfo repositoryInfo = new RepositoryInfo();

                    repositoryInfo.branch = marsh.marshall(String.class, original.get("branch"));
                    repositoryInfo.url = marsh.marshall(String.class, original.get("repoUrl"));
                    repositoryInfo.isPrivate = marsh.marshall(Boolean.class, original.get("isRepoPrivate"));
                    repositoryInfo.username = marsh.marshall(String.class, original.get("githubUsername"));
                    repositoryInfo.token = marsh.marshall(String.class, original.get("githubToken"));

                    original.put("repositoryInfo", jankson.toJson(repositoryInfo));

                    // Resource Pack Provider
                    final ResourcePackProvider resourcePackProvider = new ResourcePackProvider();

                    resourcePackProvider.enabled = true;
                    resourcePackProvider.rootLocation = marsh.marshall(String.class, original.get("resourcePackRoot"));
                    resourcePackProvider.updateMessage = marsh.marshall(String.class, original.get("packUpdateMessage"));
                    resourcePackProvider.updateMessageHoverMessage = marsh.marshall(String.class, original.get("packUpdateMessageHoverMessage"));

                    final String webhookUrl = marsh.marshall(String.class, original.get("webhookUrl"));
                    final String webhookBody = marsh.marshall(String.class, original.get("webhookBody"));

                    if (webhookUrl != null && webhookBody != null) {
                        final WebhookInfo webhookInfo = new WebhookInfo(
                                true,
                                webhookUrl,
                                webhookBody
                        );

                        resourcePackProvider.successWebhook = webhookInfo;

                        if (!webhookBody.contains("discord")) {
                            resourcePackProvider.failWebhook = webhookInfo;
                        }
                    }

                    original.put("resourcePackProvider", jankson.toJson(resourcePackProvider));
                }
        );
    }

    public void createDefaultWebhooks() {
        final Jankson jankson = new Jankson.Builder().build();
        final List<DefaultWebhookBody> webhookBodies = List.of(
                new BasicWebhook(),

                new BasicSuccessMessage(),
                new BasicFailMessage(),
                new EmbedSuccessMessage(),
                new EmbedFailMessage()
        );

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
                "http://%s:%s/%s/%s",
                serverInfo.publicIp,
                serverInfo.proxyPort == null ? PlatformServerProperties.INSTANCE.getServerPort() : serverInfo.proxyPort,
                MOD_URI,
                outputFileName
        );
    }

    public URI getWebhookUrl() {
        if (resourcePackProvider.successWebhook.url == null) return null;
        return URI.create(resourcePackProvider.successWebhook.url);
    }

    public Path getResourcePackRoot() {
        return GIT_FOLDER.resolve(config.resourcePackProvider.getRootLocation());
    }
    public Path getPacksDir() {
        return getResourcePackRoot().resolve("packs");
    }

    public String getGithubRef() {
        return "refs/heads/" + repositoryInfo.branch;
    }
}
