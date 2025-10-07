package top.offsetmonkey538.githubresourcepackmanager.platform.paper;

import org.bukkit.plugin.java.JavaPlugin;
import top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager;

public class PaperPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        PaperPlatformMain.setPlugin(this);

        GithubResourcepackManager.initialize();
    }
}
