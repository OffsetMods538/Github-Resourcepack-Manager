package top.offsetmonkey538.githubresourcepackmanager.platform.paper;

import org.bukkit.plugin.java.JavaPlugin;
import top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager;
import top.offsetmonkey538.meshlib.MeshLib;

public class PaperPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        MeshLib.initialize();

        PaperPlatformMain.setPlugin(this);
        PaperPlatformLogging.setLogger(getLogger());

        GithubResourcepackManager.initialize();
    }
}
