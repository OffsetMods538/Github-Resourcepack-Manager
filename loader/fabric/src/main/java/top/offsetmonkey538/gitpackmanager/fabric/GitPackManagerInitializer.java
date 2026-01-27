package top.offsetmonkey538.gitpackmanager.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import top.offsetmonkey538.gitpackmanager.GithubResourcepackManager;
import top.offsetmonkey538.gitpackmanager.modded.platform.ModdedPlatformMain;

public class GitPackManagerInitializer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        GithubResourcepackManager.initialize();

        ServerLifecycleEvents.SERVER_STARTING.register(ModdedPlatformMain::setServer);
    }
}
