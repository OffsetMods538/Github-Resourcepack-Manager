package top.offsetmonkey538.githubresourcepackmanager.platform.fabric;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformLogging;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformMain;

import java.nio.file.Path;
import java.util.LinkedList;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.MOD_ID;
import static top.offsetmonkey538.githubresourcepackmanager.platform.PlatformLogging.LOGGER;

public class FabricPlatformMain implements PlatformMain, DedicatedServerModInitializer {
    public static final FabricPlatformMain INSTANCE = (FabricPlatformMain) PlatformMain.INSTANCE;

    private static MinecraftServer minecraftServer;
    private static final LinkedList<Text> messageQueue = new LinkedList<>();

    @Override
    public void onInitializeServer() {
        FabricPlatformLogging.setLogger(LoggerFactory.getLogger(MOD_ID));

        GithubResourcepackManager.initialize();

        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer1 -> minecraftServer = minecraftServer1);
        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer1) -> {
            if (!minecraftServer1.getPlayerManager().isOperator(serverPlayNetworkHandler.player.getGameProfile())) return;

            for (Text text : messageQueue) {
                serverPlayNetworkHandler.player.sendMessage(text);
            }
        });
    }

    public static MinecraftServer getServer() {
        return minecraftServer;
    }


    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
    }

    @Override
    public void runOnServerStart(Runnable work) {
        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer1 -> work.run());
    }

    @Override
    public void registerLogToAdminListener() {
        LOGGER.addListener(PlatformLogging.LogLevel.ERROR, (message, error) -> {
            MutableText text = Text
                    .literal(String.format("[%s] %s", MOD_ID, message))
                    .setStyle(Style.EMPTY.withColor(Formatting.RED));
            if (error != null) text = text.setStyle(Style.EMPTY.withColor(Formatting.RED).withHoverEvent(
                    new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Text.literal(ExceptionUtils.getRootCauseMessage(error))
                    )
            ));

            boolean sent = false;
            for (final PlayerEntity player : getServer().getPlayerManager().getPlayerList()) {
                if (!getServer().getPlayerManager().isOperator(player.getGameProfile())) continue;
                player.sendMessage(text, false);
                sent = true;
            }
            if (sent) return;

            messageQueue.addLast(text);
        });
    }

    //public void registerLogToAdminListener() {
    //    LOGGER.addListener(PlatformLogging.LogLevel.ERROR, (message, error) -> {
    //        Component text = Component
    //                .text(String.format("[%s] %s", MOD_ID, message))
    //                .color(NamedTextColor.RED);
    //
    //        if (error != null) text = text.hoverEvent(
    //                HoverEvent.showText(
    //                        Component.text(ExceptionUtils.getRootCauseMessage(error))
    //                )
    //        );
    //
    //
    //        boolean sent = false;
    //        for (final OfflinePlayer operator : plugin.getServer().getOperators()) {
    //            if (operator.getPlayer() == null) continue;
    //            operator.getPlayer().sendMessage(text);
    //            sent = true;
    //        }
    //
    //        if (sent) return;
    //
    //        plugin.messageQueue.addLast(text);
    //    });
    //}
}
