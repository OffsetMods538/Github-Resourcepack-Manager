package top.offsetmonkey538.githubresourcepackmanager.platform.paper;

import net.minecraft.server.MinecraftServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.OfflinePlayer;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformMain;
import top.offsetmonkey538.monkeylib538.api.log.PlatformLogger;

import java.nio.file.Path;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.MOD_ID;
import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.LOGGER;

public class PaperPlatformMain implements PlatformMain {
    private static PaperPlugin plugin;

    @Override
    public Path getConfigDir() {
        return getPlugin().getDataPath();
    }

    @Override
    public void runOnServerStart(Runnable work) {
        work.run();
    }

    @Override
    public void refreshDatapacks() {
        MinecraftServer.getServer().getPackRepository().reload();
    }

    @Override
    public void registerLogToAdminListener() {
        LOGGER.addListener(PlatformLogger.LogLevel.ERROR, (message, error) -> {
            Component text = Component
                    .text(String.format("[%s] %s", MOD_ID, message))
                    .color(NamedTextColor.RED);

            if (error != null) text = text.hoverEvent(
                    HoverEvent.showText(
                            Component.text(ExceptionUtils.getRootCauseMessage(error))
                    )
            );


            boolean sent = false;
            for (final OfflinePlayer operator : plugin.getServer().getOperators()) {
                if (operator.getPlayer() == null) continue;
                operator.getPlayer().sendMessage(text);
                sent = true;
            }

            if (sent) return;

            plugin.messageQueue.addLast(text);
        });
    }

    public static void setPlugin(PaperPlugin plugin) {
        PaperPlatformMain.plugin = plugin;
    }

    public static PaperPlugin getPlugin() {
        return plugin;
    }
}
