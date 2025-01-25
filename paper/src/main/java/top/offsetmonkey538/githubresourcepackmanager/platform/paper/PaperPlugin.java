package top.offsetmonkey538.githubresourcepackmanager.platform.paper;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager;
import top.offsetmonkey538.meshlib.MeshLib;

import java.util.LinkedList;

public class PaperPlugin extends JavaPlugin implements Listener {
    public final LinkedList<Component> messageQueue = new LinkedList<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        MeshLib.initialize();

        PaperPlatformMain.setPlugin(this);
        PaperPlatformLogging.setLogger(getLogger());

        GithubResourcepackManager.initialize();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().isOp()) return;

        for (Component text : messageQueue) {
            event.getPlayer().sendMessage(text);
        }
        messageQueue.clear();
    }
}
