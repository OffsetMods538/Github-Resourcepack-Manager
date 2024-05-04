package top.offsetmonkey538.githubresourcepackmanager.config;

import blue.endless.jankson.Comment;
import top.offsetmonkey538.monkeylib538.config.Config;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.MOD_ID;

public class ModConfig extends Config {
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

    @Override
    protected String getName() {
        return MOD_ID;
    }
}
