package top.offsetmonkey538.gitpackmanager.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import top.offsetmonkey538.gitpackmanager.GithubResourcepackManager;
import top.offsetmonkey538.gitpackmanager.modded.platform.ModdedPlatformMain;

@Mod(
        value = "git_pack_manager",
        dist = Dist.DEDICATED_SERVER
)
public class GitPackManagerInitializer {
    public GitPackManagerInitializer(IEventBus modEventBus, ModContainer modContainer) {
        GithubResourcepackManager.initialize();

        NeoForge.EVENT_BUS.addListener(ServerStartingEvent.class, serverStartingEvent -> ModdedPlatformMain.setServer(serverStartingEvent.getServer()));
    }
}
