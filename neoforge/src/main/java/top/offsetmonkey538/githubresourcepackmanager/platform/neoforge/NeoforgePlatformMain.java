package top.offsetmonkey538.githubresourcepackmanager.platform.neoforge;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.Nullable;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformMain;
import top.offsetmonkey538.monkeylib538.api.text.MonkeyLibText;
import top.offsetmonkey538.monkeylib538.neoforge.api.text.NeoforgeMonkeyLibText;

import java.util.List;

public class NeoforgePlatformMain implements PlatformMain {
    private static @Nullable MinecraftServer minecraftServer = null;

    public static @Nullable MinecraftServer getServer() {
        return minecraftServer;
    }

    public static void setServer(MinecraftServer server) {
        minecraftServer = server;
    }


    @Override
    public void sendMessageToAdmins(MonkeyLibText message) {
        if (getServer() == null) return;
        for (final Player player : getServer().getPlayerList().getPlayers()) {
            if (!getServer().getPlayerList().isOp(player.getGameProfile())) continue;
            player.displayClientMessage(NeoforgeMonkeyLibText.of(message).getText(), false);
        }
    }

    @Override
    public void refreshDatapacks() {
        getServer().getPackRepository().reload();
    }
}
