package top.offsetmonkey538.githubresourcepackmanager.platform.paper;

import net.minecraft.server.MinecraftServer;
import org.bukkit.plugin.java.JavaPlugin;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformMain;

import java.nio.file.Path;

public class PaperPlatformMain implements PlatformMain {
    private static JavaPlugin plugin;

    @Override
    public Path getConfigDir() {
        return getPlugin().getDataPath();
    }

    @Override
    public void runOnServerStart(Runnable work) {
        work.run();
    }

    @Override
    public void refreshDatapacks() {
        MinecraftServer.getServer().getPackRepository().reload();
    }

    public static void setPlugin(JavaPlugin plugin) {
        PaperPlatformMain.plugin = plugin;
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }
}
