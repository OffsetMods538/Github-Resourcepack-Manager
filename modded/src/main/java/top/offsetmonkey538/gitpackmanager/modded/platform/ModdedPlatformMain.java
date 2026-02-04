package top.offsetmonkey538.gitpackmanager.modded.platform;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import top.offsetmonkey538.gitpackmanager.common.platform.PlatformMain;
import top.offsetmonkey538.monkeylib538.modded.api.player.ModdedPlayerApi;

import static top.offsetmonkey538.gitpackmanager.common.GitPackManager.LOGGER;

public class ModdedPlatformMain implements PlatformMain {
    private static @Nullable MinecraftServer minecraftServer = null;

    public static @Nullable MinecraftServer getServer() {
        return minecraftServer;
    }

    public static void setServer(MinecraftServer server) {
        minecraftServer = server;
    }

    @Override
    public void sendMessageToAdmins(Component message) {
        if (getServer() == null) return;
        for (final Player player : getServer().getPlayerList().getPlayers()) {
            if (!ModdedPlayerApi.isPlayerOp(getServer().getPlayerList(), player)) continue;
            ((Audience) player).sendMessage(message);
        }
    }

    @Override
    public void refreshDatapacks() {
        getServer().getPackRepository().reload();
    }

    @Override
    public void reloadEnabledDatapacks() {
        getServer().reloadResources(getServer().getPackRepository().getSelectedIds()).exceptionally(throwable -> {
            LOGGER.warn("Failed to execute reload! Keeping old data.", throwable);
            return null;
        });
    }
}
