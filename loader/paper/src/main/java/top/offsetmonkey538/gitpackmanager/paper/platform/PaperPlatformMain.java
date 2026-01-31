package top.offsetmonkey538.gitpackmanager.paper.platform;

import net.kyori.adventure.text.Component;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import top.offsetmonkey538.gitpackmanager.common.GitPackManager;
import top.offsetmonkey538.gitpackmanager.common.platform.PlatformMain;

public class PaperPlatformMain implements PlatformMain {
    @Override
    public void refreshDatapacks() {
        MinecraftServer.getServer().getPackRepository().reload();
    }

    @Override
    public void sendMessageToAdmins(Component message) {
        for (final OfflinePlayer operator : Bukkit.getServer().getOperators()) {
            if (operator.getPlayer() == null) continue;
            operator.getPlayer().sendMessage(message);
        }
    }


    public static final class GitPackManagerInitializer extends JavaPlugin {
        @Override
        public void onEnable() {
            GitPackManager.initialize();
        }
    }
}
