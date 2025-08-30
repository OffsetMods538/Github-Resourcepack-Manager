package top.offsetmonkey538.githubresourcepackmanager.config;

import blue.endless.jankson.*;
import blue.endless.jankson.api.Marshaller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformMain;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformServerProperties;
import top.offsetmonkey538.githubresourcepackmanager.utils.StringUtils;
import top.offsetmonkey538.githubresourcepackmanager.utils.WebhookSender;
import top.offsetmonkey538.offsetconfig538.api.config.Config;
import top.offsetmonkey538.offsetconfig538.api.config.Datafixer;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.*;

public class ModConfig implements Config {

    @Comment("!!!!Please check the wiki for how to set up the mod. It is linked on both the Modrinth and GitHub pages!!!!")
    public ServerInfo serverInfo = new ServerInfo();
    public RepositoryInfo repositoryInfo = new RepositoryInfo();
    public ResourcePackProvider resourcePackProvider = new ResourcePackProvider();
    public DataPackProvider dataPackProvider = new DataPackProvider();


    public static class ServerInfo {
        @SuppressWarnings("HttpUrlsUsage")
        @Comment("The public ip of your server, may also specify the protocol (\"123.45.67.89\" or \"http://play.coolserver.net\")")
        public String publicIp = null;

        @Comment("If set, this port will be used in the server.properties file instead of the Minecraft server port. The HTTP server will still be hosted on the Minecraft port. Only useful when running the server behind a proxy like nginx, traefik, cloudflare tunnel, etc.")
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

        @Comment("Messages sent in chat when pack has been updated. Each entry will be on a new line. May be 'null' or empty to disable.")
        public String[] updateMessage = new String[] {
                "&{hoverText,'{longDescription}','Server resourcepack has been updated!'}",
                "&{hoverText,'{longDescription}','Please click &{hoverText,'Click to update pack','&{runCommand,'{packUpdateCommand}','[HERE]'}'} to get the most up to date pack.'}"
        };

        @Comment("Webhook to be sent when pack updating succeeded")
        public WebhookInfo successWebhook = new WebhookInfo();
        @Comment("Webhook to be sent when pack updating failed")
        public WebhookInfo failWebhook = new WebhookInfo();

        public String getRootLocation() {
            return rootLocation.startsWith("/") ? rootLocation.substring(1) : rootLocation;
        }
        public Path getPackRoot() {
            return GIT_FOLDER.resolve(getRootLocation());
        }
        public Path getPackPacksDir() {
            return getPackRoot().resolve("packs");
        }
    }

    public static class DataPackProvider {
        @Comment("Whether or not the data pack provider is enabled. Default: false")
        public boolean enabled = false;
        @Comment("Where the mod will search for data packs in the cloned repository. MUST NOT be same as or child of the 'rootLocation' of the resourcepack provider")
        public String rootLocation = "/datapacks";
        @Comment("Messages sent TO ADMINS in chat when pack has been updated. Each entry will be on a new line. May be 'null' or empty to disable.")
        public String[] updateMessage = new String[] {
                "&{hoverText,'{longDescription}','Server datapacks have been updated!'}",
                "&{hoverText,'{longDescription}','New packs (if any) will need to be enabled with the &{hoverText,'Click to suggest','&{suggestCommand,'/datapack enable','&n/datapack enable'}'} command.'}",
                "&{hoverText,'{longDescription}','Please run &{hoverText,'Click to suggest','&{suggestCommand,'/reload','&n/reload'}'} or restart the server to reload datapacks.'}"
        };

        @Comment("Webhook to be sent when pack updating succeeded")
        public WebhookInfo successWebhook = new WebhookInfo();
        @Comment("Webhook to be sent when pack updating failed")
        public WebhookInfo failWebhook = new WebhookInfo();

        public String getRootLocation() {
            return rootLocation.startsWith("/") ? rootLocation.substring(1) : rootLocation;
        }
        public Path getPackRoot() {
            return GIT_FOLDER.resolve(getRootLocation());
        }
        public Path getPackPacksDir() {
            return getPackRoot().resolve("packs");
        }
    }

    public static class WebhookInfo {
        public WebhookInfo() {

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
                webhookBody = StringUtils.replacePlaceholders(webhookBody, placeholders, false, true);

                WebhookSender.send(webhookBody, getWebhookUrl(), updateType, updateSucceeded);
            } catch (IOException e) {
                throw new GithubResourcepackManagerException("Failed to read content of webhook body file '%s'!", e, config.get().resourcePackProvider.successWebhook.body);
            }
        }

        public @Nullable URI getWebhookUrl() {
            if (url == null) return null;
            return URI.create(url);
        }

        public @Nullable Path getBodyPath() {
            return body == null ? null : config.get().getFilePath().getParent().resolve(body);
        }
    }

    @Override
    public @NotNull Datafixer[] getDatafixers() {
        return new Datafixer[]{
                (original, jankson) -> {
                    // 0 -> 1
                    original.put("branch", jankson.toJson(jankson.getMarshaller().marshall(String.class, original.get("githubRef")).replace("refs/heads/", "")));
                    datafixField(original, "githubUrl", "repoUrl");
                    datafixField(original, "isPrivate", "isRepoPrivate");
                },
                (original, jankson) -> {
                    // 1 -> 2
                    // noop
                },
                (original, jankson) -> {
                    // 2 -> 3

                    final Marshaller marsh = jankson.getMarshaller();

                    // Server Info
                    final JsonObject serverInfo = new JsonObject();

                    datafixField(original, "serverPublicIp", serverInfo, "publicIp");
                    datafixField(original, "proxyPort", serverInfo, "proxyPort");

                    original.put("serverInfo", serverInfo);

                    // Repository Info
                    final JsonObject repositoryInfo = new JsonObject();

                    datafixField(original, "branch", repositoryInfo, "branch");
                    datafixField(original, "repoUrl", repositoryInfo, "url");

                    datafixField(original, "isRepoPrivate", repositoryInfo, "isPrivate");

                    datafixField(original, "githubUsername", repositoryInfo, "username");
                    datafixField(original, "githubToken", repositoryInfo, "token");

                    original.put("repositoryInfo", repositoryInfo);

                    // Resource Pack Provider
                    final JsonObject resourcePackProvider = new JsonObject();

                    datafixField(original, "resourcePackRoot", serverInfo, "rootLocation");

                    datafixField(original, "packUpdateMessage", serverInfo, "updateMessage");
                    datafixField(original, "packUpdateMessageHoverMessage", serverInfo, "updateMessageHoverMessage");

                    final String webhookUrl = datafixGetAndRemove(marsh, String.class, original, "webhookUrl");
                    final String webhookBody = datafixGetAndRemove(marsh, String.class, original, "webhookBody");
                    if (webhookUrl != null && webhookBody != null) {
                        final JsonObject webhookInfo = new JsonObject();
                        webhookInfo.put("enabled", jankson.toJson(true));
                        webhookInfo.put("url", jankson.toJson(webhookUrl));
                        webhookInfo.put("body", jankson.toJson(webhookBody));

                        resourcePackProvider.put("successWebhook", webhookInfo);
                        if (!webhookBody.contains("discord")) resourcePackProvider.put("failWebhook", webhookInfo);
                    }

                    original.put("resourcePackProvider", resourcePackProvider);
                },
                (original, jankson) -> {
                    // 3 -> 4
                    final Marshaller marsh = jankson.getMarshaller();

                    {
                        final JsonObject resourcePackProvider = (JsonObject) original.get("resourcePackProvider");
                        assert resourcePackProvider != null;

                        final String updateMessage = datafixGetAndRemove(marsh, String.class, resourcePackProvider, "updateMessage");
                        if (updateMessage == null) {
                            original.put("updateMessage", JsonNull.INSTANCE);
                            return;
                        }

                        final String updateMessageHoverMessage = datafixGetAndRemove(marsh, String.class, resourcePackProvider, "updateMessageHoverMessage");
                        resourcePackProvider.remove("updateMessageHoverMessage");

                        final String[] newUpdateMessage = updateMessage.split("\\n");
                        for (int i = 0; i < newUpdateMessage.length; i++) {
                            newUpdateMessage[i] = newUpdateMessage[i].replaceAll("\\{packUpdateCommand}", "&{hoverText,'Click to update pack','&{runCommand,'{packUpdateCommand}','[HERE]'}'}");
                            if (updateMessageHoverMessage != null)
                                newUpdateMessage[i] = "&{hoverText,'%s','%s'}".formatted(updateMessageHoverMessage, newUpdateMessage[i]);
                        }
                        resourcePackProvider.put("updateMessage", new JsonArray(newUpdateMessage, marsh));

                        original.put("resourcePackProvider", resourcePackProvider);
                    }
                    {

                        final JsonObject dataPackProvider = (JsonObject) original.get("dataPackProvider");
                        assert dataPackProvider != null;

                        final String updateMessage = datafixGetAndRemove(marsh, String.class, dataPackProvider, "updateMessage");
                        if (updateMessage == null) {
                            original.put("updateMessage", JsonNull.INSTANCE);
                            return;
                        }

                        final String updateMessageHoverMessage = datafixGetAndRemove(marsh, String.class, dataPackProvider, "updateMessageHoverMessage");
                        dataPackProvider.remove("updateMessageHoverMessage");

                        final String[] newUpdateMessage = updateMessage.split("\\n");
                        for (int i = 0; i < newUpdateMessage.length; i++) {
                            newUpdateMessage[i] =  newUpdateMessage[i].replaceAll("\\{packUpdateCommand}", "&{hoverText,'Click to update pack','&{runCommand,'{packUpdateCommand}','[HERE]'}'}");
                            if (updateMessageHoverMessage != null) newUpdateMessage[i] = "&{hoverText,'%s','%s'}".formatted(updateMessageHoverMessage, newUpdateMessage[i]);
                        }
                        dataPackProvider.put("updateMessage", new JsonArray(newUpdateMessage, marsh));

                        original.put("dataPackProvider", dataPackProvider);
                    }
                }
        };
    }

    private static void datafixField(final @NotNull JsonObject originalObject, final @NotNull String originalKey, final @NotNull String newKey) {
        datafixField(originalObject, originalKey, originalObject, newKey);
    }
    private static void datafixField(final @NotNull JsonObject originalObject, final @NotNull String originalKey, final @NotNull JsonObject newObject, final @NotNull String newKey) {
        final JsonElement originalValue = originalObject.get(originalKey);
        if (originalValue == null) {
            LOGGER.warn("JSON of current config doesn't contain value with key '%s', new value with key '%s' will be reset to default!", originalKey, newKey);
            return;
        }

        newObject.put(newKey, originalValue);
        originalObject.remove(originalKey);
    }

    @Nullable
    private static <T> T datafixGetAndRemove(final @NotNull Marshaller marsh, final @NotNull Class<T> type, final @NotNull JsonObject object, final @NotNull String key) {
        final JsonElement jsonValue = object.get(key);
        if (jsonValue == null) {
            LOGGER.warn("JSON of current config doesn't contain value with key '%s'!", key);
            return null;
        }
        object.remove(key);
        return marsh.marshall(type, jsonValue);
    }

    @Override
    public int getConfigVersion() {
        return 4;
    }

    @Override
    public @NotNull Path getFilePath() {
        return PlatformMain.INSTANCE.getConfigDir().resolve("config").resolve("main.json");
    }

    @Override
    public @NotNull String getId() {
        return MOD_ID + "/main";
    }

    @Override
    public void beforeLoadStart() {
        if (Files.exists(PlatformMain.INSTANCE.getConfigDir().getParent().resolve(MOD_ID + ".json"))) {
            try {
                Files.createDirectories(PlatformMain.INSTANCE.getConfigDir().resolve(MOD_ID + ".json").getParent());
                Files.move(PlatformMain.INSTANCE.getConfigDir().getParent().resolve(MOD_ID + ".json"), PlatformMain.INSTANCE.getConfigDir().resolve(MOD_ID + ".json"));
            } catch (IOException e) {
                throw new RuntimeException("Failed to move config file to new location!", e);
            }
        }
        if (Files.exists(PlatformMain.INSTANCE.getConfigDir().resolve(MOD_ID + ".json"))) {
            try {
                Files.createDirectories(getFilePath().getParent());
                Files.move(PlatformMain.INSTANCE.getConfigDir().resolve(MOD_ID + ".json"), getFilePath());
            } catch (IOException e) {
                throw new RuntimeException("Failed to move config file to new location!", e);
            }
        }
    }

    @SuppressWarnings("HttpUrlsUsage")
    public String getPackUrl(String outputFileName) {
        return String.format(
                "%s%s:%s/%s/%s",
                (serverInfo.publicIp.startsWith("http://") || serverInfo.publicIp.startsWith("https://") ? "" : "http://"),
                serverInfo.publicIp,
                serverInfo.proxyPort == null ? PlatformServerProperties.INSTANCE.getServerPort() : serverInfo.proxyPort,
                MOD_URI,
                outputFileName
        );
    }

    public String getGithubRef() {
        return "refs/heads/" + repositoryInfo.branch;
    }
}
