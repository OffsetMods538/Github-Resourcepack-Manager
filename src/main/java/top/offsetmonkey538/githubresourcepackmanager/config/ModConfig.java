package top.offsetmonkey538.githubresourcepackmanager.config;

import blue.endless.jankson.Comment;
import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonPrimitive;
import top.offsetmonkey538.monkeylib538.config.Config;

import java.net.URI;
import java.nio.file.Path;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.MOD_ID;

public class ModConfig extends Config {
    @Comment("!!!!Please check the wiki for how to set up the mod. It is linked on both the Modrinth and GitHub pages!!!!")
    public String packUpdateMessage = "Server resourcepack has been updated!\nPlease rejoin the server to get the most up to date pack.";
    @Comment("The port that the *webserver* binds to. *NOT* the same as your minecraft servers port")
    public int webServerBindPort = 8080;
    @Comment("Usually shouldn't need changing")
    public String webServerBindIp = "0.0.0.0";
    @Comment("Usually shouldn't need changing")
    public String webhookPath = "/webhook";
    @Comment("The public ip of your server (123.45.67.89 or play.coolserver.net)")
    public String serverPublicIp = null;
    @Comment("Should be \"refs/heads/[YOUR BRANCH NAME HERE]\"")
    public String githubRef = "refs/heads/master";
    public String githubUrl = null;
    public boolean isPrivate = false;
    public String githubUsername = null;
    @Comment("PLEASE DON'T SHARE THIS WITH ANYONE EVER")
    public String githubToken = null;
    public URI webhookUrl = null;
    public Path webhookBody = null;

    @Override
    protected String getName() {
        return MOD_ID + "/" + MOD_ID;
    }

    @Override
    protected Jankson.Builder configureJankson(Jankson.Builder builder) {
        builder.registerSerializer(URI.class, (uri, marsh) -> marsh.serialize(uri.toString()));
        builder.registerDeserializer(JsonPrimitive.class, URI.class, (json, marsh) -> URI.create(marsh.marshall(String.class, json)));

        builder.registerSerializer(Path.class, (path, marsh) -> marsh.serialize(path.toString()));
        builder.registerDeserializer(JsonPrimitive.class, Path.class, (json, marsh) -> getFilePath().getParent().resolve(marsh.marshall(String.class, json)));

        return builder;
    }

    public String getPackUrl(String outputFileName) {
        return String.format(
                "http://%s:%s/%s",
                serverPublicIp,
                webServerBindPort,
                outputFileName
        );
    }
}
