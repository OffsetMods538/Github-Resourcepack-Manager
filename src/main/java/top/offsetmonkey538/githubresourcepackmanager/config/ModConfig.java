package top.offsetmonkey538.githubresourcepackmanager.config;

import blue.endless.jankson.Comment;
import top.offsetmonkey538.monkeylib538.config.Config;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.MOD_ID;

public class ModConfig extends Config {
    public String updateMessage = "Server resourcepack has been updated!\nPlease rejoin the server to get the most up to date pack.";
    public int serverPort = 8080;
    @Comment("Usually shouldn't need changing")
    public String serverIp = "0.0.0.0";
    @Comment("Usually shouldn't need changing")
    public String webhookPath = "/webhook";
    public String githubRef = "refs/heads/master";
    public String githubUrl = null;
    @Comment("This is *not* the same as githubUrl!")
    public String resourcepackUrl = null;
    public boolean isPrivate = false;
    public String githubUsername = null;
    @Comment("PLEASE DON'T SHARE THIS WITH ANYONE EVER")
    public String githubToken = null;

    @Override
    protected String getName() {
        return MOD_ID;
    }
}
