package top.offsetmonkey538.githubresourcepackmanager.platform.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.slf4j.Logger;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformMain;

import java.nio.file.Path;

public class FabricPlatformMain implements PlatformMain, DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {

    }


    @Override
    public Logger getLogger() {
        return null;
    }

    @Override
    public Path getConfigDir() {
        return null;
    }

    @Override
    public Path getGameDir() {
        return null;
    }

    @Override
    public void runOnServerStart(Runnable work) {

    }
}
