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

import java.util.List;

public class PaperPlatformMain implements PlatformMain {
    private static PaperPlugin plugin;


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

    public static void setPlugin(PaperPlugin plugin) {
        PaperPlatformMain.plugin = plugin;
    }

    public static PaperPlugin getPlugin() {
        return plugin;
    }
}
