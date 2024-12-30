package top.offsetmonkey538.githubresourcepackmanager.platform.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformMain;

import java.nio.file.Path;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.MOD_ID;

public class FabricPlatformMain implements PlatformMain, DedicatedServerModInitializer {
    public static final FabricPlatformMain INSTANCE = (FabricPlatformMain) PlatformMain.INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static MinecraftServer minecraftServer;

    @Override
    public void onInitializeServer() {
        GithubResourcepackManager.initialize();

        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer1 -> minecraftServer = minecraftServer1);
    }

    public MinecraftServer getServer() {
        return minecraftServer;
    }


    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public void runOnServerStart(Runnable work) {
        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer1 -> work.run());
    }
}
