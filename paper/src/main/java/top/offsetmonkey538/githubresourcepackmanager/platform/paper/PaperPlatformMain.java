package top.offsetmonkey538.githubresourcepackmanager.platform.paper;

import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformMain;
import top.offsetmonkey538.monkeylib538.api.text.MonkeyLibText;

import java.nio.file.Path;
import java.util.List;

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
    public void registerSendMessageQueueOnAdminJoin(List<MonkeyLibText> messageQueue, MonkeyLibText lastMessage) {
        record AdminMessageQueueEventHandler(List<MonkeyLibText> messageQueue) implements Listener {
            @EventHandler(priority = EventPriority.MONITOR)
            public void onPlayerJoin(PlayerJoinEvent event) {
                if (messageQueue.isEmpty()) return;
                if (!event.getPlayer().isOp()) return;

                for (MonkeyLibText text : messageQueue) {
                    // todo: once monkeylib has paper support: event.getPlayer().sendMessage(PaperMonkeyLibText.of(text).getText());
                }
                // todo: once monkeylib has paper support: event.getPlayer().sendMessage(PaperMonkeyLibText.of(lastMessage).getText());
            }
        }

        Bukkit.getPluginManager().registerEvents(new AdminMessageQueueEventHandler(messageQueue), getPlugin());
    }

    public static void setPlugin(PaperPlugin plugin) {
        PaperPlatformMain.plugin = plugin;
    }

    public static PaperPlugin getPlugin() {
        return plugin;
    }
}
