package top.offsetmonkey538.githubresourcepackmanager.platform.paper;

import net.minecraft.server.MinecraftServer;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformMain;
import top.offsetmonkey538.monkeylib538.api.text.MonkeyLibText;

public class PaperPlatformMain implements PlatformMain {
    private static GitPackManagerInitializer plugin;

    @Override
    public void refreshDatapacks() {
        MinecraftServer.getServer().getPackRepository().reload();
    }

    @Override
    public void sendMessageToAdmins(MonkeyLibText message) {
        for (final OfflinePlayer operator : getPlugin().getServer().getOperators()) {
            if (operator.getPlayer() == null) continue;
            // todo: once monkeylib has da paper: operator.getPlayer().sendMessage(PaperMonkeyLibText.of(text));
        }
    }

    private static void setPlugin(GitPackManagerInitializer plugin) {
        PaperPlatformMain.plugin = plugin;
    }

    public static GitPackManagerInitializer getPlugin() {
        return plugin;
    }


    public static final class GitPackManagerInitializer extends JavaPlugin {
        @Override
        public void onEnable() {
            PaperPlatformMain.setPlugin(this);
            GithubResourcepackManager.initialize();
        }
    }

}
