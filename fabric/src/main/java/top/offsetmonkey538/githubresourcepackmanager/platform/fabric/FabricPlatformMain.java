package top.offsetmonkey538.githubresourcepackmanager.platform.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformMain;
import top.offsetmonkey538.monkeylib538.api.text.MonkeyLibText;
import top.offsetmonkey538.monkeylib538.fabric.api.player.FabricPlayerApi;
import top.offsetmonkey538.monkeylib538.fabric.api.text.FabricMonkeyLibText;

import java.util.List;

public class FabricPlatformMain implements PlatformMain, DedicatedServerModInitializer {
    private static @Nullable MinecraftServer minecraftServer = null;

    @Override
    public void onInitializeServer() {
        GithubResourcepackManager.initialize();

        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer1 -> minecraftServer = minecraftServer1);
    }

    public static @Nullable MinecraftServer getServer() {
        return minecraftServer;
    }


    @Override
    public void sendMessageToAdmins(MonkeyLibText message) {
        if (getServer() == null) return;
        for (final PlayerEntity player : getServer().getPlayerManager().getPlayerList()) {
            if (!FabricPlayerApi.isPlayerOp(getServer().getPlayerManager(), player)) continue;
            player.sendMessage(FabricMonkeyLibText.of(message).getText(), false);
        }
    }

    @Override
    public void registerSendMessageQueueOnAdminJoin(List<MonkeyLibText> messageQueue, MonkeyLibText lastMessage) {
        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer1) -> {
            if (messageQueue.isEmpty()) return;
            if (!FabricPlayerApi.isPlayerOp(minecraftServer1.getPlayerManager(), serverPlayNetworkHandler.player)) return;

            for (MonkeyLibText text : messageQueue) serverPlayNetworkHandler.player.sendMessage(FabricMonkeyLibText.of(text).getText());
            serverPlayNetworkHandler.player.sendMessage(FabricMonkeyLibText.of(lastMessage).getText());
        });
    }

    @Override
    public void refreshDatapacks() {
        getServer().getDataPackManager().scanPacks();
    }
}
