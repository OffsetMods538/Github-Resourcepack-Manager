package top.offsetmonkey538.githubresourcepackmanager.platform.neoforge;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.jetbrains.annotations.Nullable;
import top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformMain;
import top.offsetmonkey538.monkeylib538.api.text.MonkeyLibText;
import top.offsetmonkey538.monkeylib538.neoforge.api.text.NeoforgeMonkeyLibText;

import java.nio.file.Path;
import java.util.List;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.MOD_ID;

public class NeoforgePlatformMain implements PlatformMain {
    private static @Nullable MinecraftServer minecraftServer = null;

    public static @Nullable MinecraftServer getServer() {
        return minecraftServer;
    }

    public static void setServer(MinecraftServer server) {
        minecraftServer = server;
    }


    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get().resolve(MOD_ID);
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
    public void registerSendMessageQueueOnAdminJoin(List<MonkeyLibText> messageQueue, MonkeyLibText lastMessage) {
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, playerLoggedInEvent -> {
            if (messageQueue.isEmpty()) return;
            if (!playerLoggedInEvent.getEntity().getServer().getPlayerList().isOp(playerLoggedInEvent.getEntity().getGameProfile())) return;

            for (MonkeyLibText text : messageQueue) playerLoggedInEvent.getEntity().displayClientMessage(NeoforgeMonkeyLibText.of(text).getText(), false);
            playerLoggedInEvent.getEntity().displayClientMessage(NeoforgeMonkeyLibText.of(lastMessage).getText(), false);
        });
    }

    @Override
    public void refreshDatapacks() {
        getServer().getPackRepository().reload();
    }
}
