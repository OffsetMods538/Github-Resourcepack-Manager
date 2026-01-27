package top.offsetmonkey538.gitpackmanager.modded.platform;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import top.offsetmonkey538.gitpackmanager.platform.PlatformMain;
import top.offsetmonkey538.monkeylib538.common.api.text.MonkeyLibText;
import top.offsetmonkey538.monkeylib538.modded.api.text.ModdedMonkeyLibText;

public class ModdedPlatformMain implements PlatformMain {
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
            // TODO: GAME PROFILE ISNT CORRETCT: if (!getServer().getPlayerList().isOp(player.getGameProfile())) continue;
            player.displayClientMessage(ModdedMonkeyLibText.of(message).getText(), false);
        }
    }

    @Override
    public void refreshDatapacks() {
        getServer().getPackRepository().reload();
    }
}
