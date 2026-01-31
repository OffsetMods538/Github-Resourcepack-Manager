package top.offsetmonkey538.gitpackmanager.config;

import blue.endless.jankson.Comment;
import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonNull;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.Marshaller;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jspecify.annotations.Nullable;
import top.offsetmonkey538.gitpackmanager.exception.GitPackManagerException;
import top.offsetmonkey538.gitpackmanager.utils.StringUtils;
import top.offsetmonkey538.gitpackmanager.utils.WebhookSender;
import top.offsetmonkey538.meshlib.common.api.MESHLibApi;
import top.offsetmonkey538.meshlib.common.api.rule.HttpRule;
import top.offsetmonkey538.meshlib.common.api.rule.rules.PathHttpRule;
import top.offsetmonkey538.monkeylib538.common.api.platform.LoaderUtil;
import top.offsetmonkey538.monkeylib538.common.api.text.TextFormattingApi;
import top.offsetmonkey538.offsetutils538.api.config.Config;
import top.offsetmonkey538.offsetutils538.api.config.Datafixer;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static top.offsetmonkey538.gitpackmanager.GitPackManager.GIT_FOLDER;
import static top.offsetmonkey538.gitpackmanager.GitPackManager.LOGGER;
import static top.offsetmonkey538.gitpackmanager.GitPackManager.MOD_ID;
import static top.offsetmonkey538.gitpackmanager.GitPackManager.UpdateType;
import static top.offsetmonkey538.gitpackmanager.GitPackManager.config;
import static top.offsetmonkey538.offsetutils538.api.text.ArgReplacer.replaceArgs;

public final class ModConfig implements Config {

    @Comment("!!!! Please check the wiki for how to set up the mod. It should be available here: https://git-pack-manager.docs.offsetmonkey538.top and is also linked on the Modrinth page. !!!!")
    public ServerInfo serverInfo = new ServerInfo();
    public RepositoryInfo repositoryInfo = new RepositoryInfo();
    public ResourcePackProvider resourcePackProvider = new ResourcePackProvider();
    public DataPackProvider dataPackProvider = new DataPackProvider();


    public static class ServerInfo {
        @Comment("The MESH Lib rule to use for routing to the Git Pack Manager http handler. See MESH Lib docs: https://mesh-lib.docs.offsetmonkey538.top/WHATEVER/PAGE/WILL/TELL/YOU/HOW/TO/USE/RULES") // TODO: real link once meshlib has docs
        public HttpRule routingRule = new PathHttpRule(MOD_ID);
        @Comment("The public ip of your server. (\"123.45.67.89\" or \"play.coolserver.net\")")
        public @Nullable String publicIp = null;
        @SuppressWarnings("HttpUrlsUsage")
        @Comment("The url matched by the routing rule defined above. May include placeholders for \"{exposedPort}\" (defined in MESH Lib config), \"{publicIp}\" (defined above) and \"{filename}\" (the file to download). Used for generating the download url for clients. Default value: \"http://{publicIp}:{exposedPort}/" + MOD_ID + "/{filename}\"")
        public String downloadUrlPattern = "http://{publicIp}:{exposedPort}/" + MOD_ID + "/{filename}";
    }

    public static class RepositoryInfo {
        @Comment("Should be \"[YOUR BRANCH NAME HERE]\". Common names include \"master\" and \"main\"")
        public String branch = "master";
        @Comment("The URL of your repository. For example \"https://github.com/MyName/MyRepository\"")
        public @Nullable String url = null;

        @Comment("Whether or not the repository is private. Username and token will need to be populated when this is set to 'true'!")
        public boolean isPrivate = false;

        @Comment("The two values below only need to be set when 'isPrivate' is true!")
        public @Nullable String username = null;
        @Comment("PLEASE DO NOT SHARE THIS WITH ANYONE")
        public @Nullable String token = null;
    }

    public static class ResourcePackProvider {
        @Comment("Whether or not the resource pack provider is enabled. Default: true")
        public boolean enabled = true;
        @Comment("Where the mod will search for resource packs in the cloned repository. MUST NOT be same as or child of the 'rootLocation' of the datapack provider")
        public String rootLocation = "/resourcepacks";

        @Comment("Messages sent in chat when pack has been updated. Each entry will be on a new line. May be 'null' or empty to disable.")
        public @Nullable String[] updateMessage = new String[] {
                "<hover:show_text:'{longDescription}'>Server resource pack has been updated!</hover>",
                "<hover:show_text:'{longDescription}'>Please click <hover:show_text:'Click to update pack'><click:run_command:/git-pack-manager request-pack>[HERE]</click></hover> to get the most up to date pack.</hover>"
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
                "<hover:show_text:'{longDescription}'>Server datapacks have been updated!</hover>",
                "<hover:show_text:'{longDescription}'>New packs (if any) will need to be enabled with the <hover:show_text:'Click to suggest'><click:suggest_command:/datapack enable><underlined>/datapack enable</underlined></click></hover> command.</hover>",
                "<hover:show_text:'{longDescription}'>Please run <hover:show_text:'Click to suggest'><click:suggest_command:/reload><underlined>/reload</underlined></click></hover> or restart the server to reload datapacks.</hover>"
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
        public @Nullable String url = null;
        @Comment("The relative path from the config directory to a webhook body file. For example \"discord/basic_message.json\" or \"discord/embed_message.json\"")
        public @Nullable String body = null;

        public void trigger(final boolean updateSucceeded, final Map<String, String> placeholders, final UpdateType updateType) throws GitPackManagerException {
            if (!enabled) return;

            try {
                //noinspection DataFlowIssue: Only returns null when `body` is null, which we have already checked
                String webhookBody = Files.readString(getBodyPath());
                webhookBody = StringUtils.replacePlaceholders(webhookBody, placeholders, false, true);

                WebhookSender.send(webhookBody, getWebhookUrl(), updateType, updateSucceeded);
            } catch (IOException e) {
                throw new GitPackManagerException("Failed to read content of webhook body file '%s'!", e, config.get().resourcePackProvider.successWebhook.body);
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
    public Datafixer[] getDatafixers() {
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

                    original.put("resourcePackProvider", datafix3to4UpdateMessage((JsonObject) original.get("resourcePackProvider"), marsh));
                    original.put("dataPackProvider", datafix3to4UpdateMessage((JsonObject) original.get("dataPackProvider"), marsh));
                },
                (original, jankson) -> {
                    // 4 -> 5
                    final JsonObject serverInfo = original.getObject("serverInfo");
                    serverInfo.remove("proxyPort");
                    original.put("serverInfo", serverInfo);

                    final Marshaller marsh = jankson.getMarshaller();

                    original.put("resourcePackProvider", datafix4to5UpdateMessage((JsonObject) original.get("resourcePackProvider"), marsh));
                    original.put("dataPackProvider", datafix4to5UpdateMessage((JsonObject) original.get("dataPackProvider"), marsh));
                }
        };
    }

    private static void datafixField(final JsonObject originalObject, final String originalKey, final String newKey) {
        datafixField(originalObject, originalKey, originalObject, newKey);
    }
    private static void datafixField(final JsonObject originalObject, final String originalKey, final JsonObject newObject, final String newKey) {
        final JsonElement originalValue = originalObject.get(originalKey);
        if (originalValue == null) {
            LOGGER.warn("JSON of current config doesn't contain value with key '%s', new value with key '%s' will be reset to default!", originalKey, newKey);
            return;
        }

        newObject.put(newKey, originalValue);
        originalObject.remove(originalKey);
    }

    @Nullable
    private static <T> T datafixGetAndRemove(final Marshaller marsh, final Class<T> type, final JsonObject object, final String key) {
        final JsonElement jsonValue = object.get(key);
        if (jsonValue == null) {
            LOGGER.warn("JSON of current config doesn't contain value with key '%s'!", key);
            return null;
        }
        object.remove(key);
        return marsh.marshall(type, jsonValue);
    }

    private static JsonObject datafix3to4UpdateMessage(final JsonObject originalJson, final Marshaller marsh) {
        final String updateMessage = datafixGetAndRemove(marsh, String.class, originalJson, "updateMessage");
        if (updateMessage == null) {
            originalJson.put("updateMessage", JsonNull.INSTANCE);
            return originalJson;
        }

        final String updateMessageHoverMessage = datafixGetAndRemove(marsh, String.class, originalJson, "updateMessageHoverMessage");

        final String[] newUpdateMessage = updateMessage.split("\\n");
        for (int i = 0; i < newUpdateMessage.length; i++) {
            newUpdateMessage[i] = newUpdateMessage[i].replaceAll("\\{packUpdateCommand}", "&{hoverText,'Click to update pack','&{runCommand,'{packUpdateCommand}','[HERE]'}'}");
            if (updateMessageHoverMessage != null)
                newUpdateMessage[i] = replaceArgs("&{hoverText,'%s','%s'}", updateMessageHoverMessage, newUpdateMessage[i].replace("'", "\\'"));
        }
        originalJson.put("updateMessage", new JsonArray(newUpdateMessage, marsh));

        return originalJson;
    }

    private static JsonObject datafix4to5UpdateMessage(final JsonObject originalJson, final Marshaller marsh) {
        final JsonArray updateMessageJson = ((JsonArray) originalJson.get("updateMessage"));
        if (updateMessageJson == null) {
            originalJson.put("updateMessage", JsonNull.INSTANCE);
            return originalJson;
        }

        final String[] updateMessage = new String[updateMessageJson.size()];

        for (int i = 0; i < updateMessage.length; i++) {
            try {
                //noinspection deprecation
                updateMessage[i] = MiniMessage.miniMessage().serialize(TextFormattingApi.styleText(updateMessageJson.getString(i, "").replace("{packUpdateCommand}", "/git-pack-manager request-pack")));
            } catch (Exception e) {
                LOGGER.error("Failed to migrate update message at line %s!", e, i);
            }
        }

        originalJson.put("updateMessage", new JsonArray(updateMessage, marsh));

        return originalJson;
    }

    @Override
    public int getConfigVersion() {
        return 5;
    }

    @Override
    public Path getConfigDirPath() {
        return LoaderUtil.getConfigDir();
    }

    @Override
    public String getId() {
        return MOD_ID + "/main";
    }

    @Override
    public void beforeLoadStart() {
        tryMoveConfig(
                LoaderUtil.getConfigDir().resolve("github-resourcepack-manager.json"),
                LoaderUtil.getConfigDir().resolve("github-resourcepack-manager").resolve("github-resourcepack-manager.json")
        );
        tryMoveConfig(
                LoaderUtil.getConfigDir().resolve("github-resourcepack-manager").resolve("github-resourcepack-manager.json"),
                LoaderUtil.getConfigDir().resolve("github-resourcepack-manager/main.json")
        );
        tryMoveConfig(
                LoaderUtil.getConfigDir().resolve("github-resourcepack-manager/main.json"),
                getFilePath()
        );
    }

    private static void tryMoveConfig(final Path oldPath, final Path newPath) {
        if (Files.exists(oldPath)) {
            try {
                Files.createDirectories(newPath.getParent());
                Files.move(oldPath, newPath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to move config file to new location!", e);
            }
        }
    }

    public String getPackUrl(String outputFileName) {
        return serverInfo.downloadUrlPattern
                .replace("{publicIp}", serverInfo.publicIp)
                .replace("{exposedPort}", String.valueOf(MESHLibApi.getExternalPort()))
                .replace("{filename}", outputFileName);
    }

    public String getGithubRef() {
        return "refs/heads/" + repositoryInfo.branch;
    }
}
