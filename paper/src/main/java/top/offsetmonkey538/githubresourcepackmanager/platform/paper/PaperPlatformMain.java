package top.offsetmonkey538.githubresourcepackmanager.platform.paper;

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

    public static void setPlugin(JavaPlugin plugin) {
        PaperPlatformMain.plugin = plugin;
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }


    //@Override
    //public void onInitializeServer() {
    //    GithubResourcepackManager.initialize();

    //    ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer1 -> minecraftServer = minecraftServer1);
    //}

    //public MinecraftServer getServer() {
    //    return minecraftServer;
    //}


    //@Override
    //public Logger getLogger() {
    //    return LOGGER;
    //}

    //@Override
    //public Path getConfigDir() {
    //    return FabricLoader.getInstance().getConfigDir();
    //}

    //@Override
    //public Path getGameDir() {
    //    return FabricLoader.getInstance().getGameDir();
    //}

    //@Override
    //public void runOnServerStart(Runnable work) {
    //    ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer1 -> work.run());
    //}
}
