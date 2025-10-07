package top.offsetmonkey538.githubresourcepackmanager.platform.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager;

@Mod(
        value = "git_pack_manager",
        dist = Dist.DEDICATED_SERVER
)
public class NeoforgePlatformInitializer {

    public NeoforgePlatformInitializer(IEventBus modEventBus, ModContainer modContainer) {
        GithubResourcepackManager.initialize();

        NeoForge.EVENT_BUS.addListener(ServerStartingEvent.class, serverStartingEvent -> NeoforgePlatformMain.setServer(serverStartingEvent.getServer()));
    }
}
