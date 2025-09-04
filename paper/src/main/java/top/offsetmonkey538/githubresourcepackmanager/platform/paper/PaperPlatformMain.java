package top.offsetmonkey538.githubresourcepackmanager.platform.paper;

import net.kyori.adventure.text.ComponentLike;
import net.minecraft.server.MinecraftServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformMain;
import top.offsetmonkey538.monkeylib538.api.log.MonkeyLibLogger;
import top.offsetmonkey538.monkeylib538.api.text.MonkeyLibText;

import java.nio.file.Path;
import java.util.List;

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
    public void sendMessageToAdmins(MonkeyLibText message) {
        for (final OfflinePlayer operator : plugin.getServer().getOperators()) {
            if (operator.getPlayer() == null) continue;
            // todo: once monkeylib has da paper: operator.getPlayer().sendMessage(PaperMonkeyLibText.of(text));
        }
    }

    @Override
    public void registerSendMessageQueueOnAdminJoin(List<MonkeyLibText> messageQueue) {
        record AdminMessageQueueEventHandler(List<MonkeyLibText> messageQueue) implements Listener {
            @EventHandler(priority = EventPriority.MONITOR)
            public void onPlayerJoin(PlayerJoinEvent event) {
                if (!event.getPlayer().isOp()) return;

                for (MonkeyLibText text : messageQueue) {
                    // todo: once monkeylib has paper support: event.getPlayer().sendMessage(PaperMonkeyLibText.of(text));
                }
            }
        }

        Bukkit.getPluginManager().registerEvents(new AdminMessageQueueEventHandler(messageQueue), getPlugin());
    }

    public void sendMessageToAdmins() {
        LOGGER.addListener(MonkeyLibLogger.LogLevel.ERROR, (message, error) -> {
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
