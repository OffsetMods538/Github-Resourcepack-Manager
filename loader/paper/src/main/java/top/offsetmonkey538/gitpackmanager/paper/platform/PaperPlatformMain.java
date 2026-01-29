package top.offsetmonkey538.gitpackmanager.paper.platform;

import net.minecraft.server.MinecraftServer;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;
import top.offsetmonkey538.gitpackmanager.GitPackManager;
import top.offsetmonkey538.gitpackmanager.platform.PlatformMain;
import top.offsetmonkey538.monkeylib538.common.api.text.MonkeyLibText;
import top.offsetmonkey538.monkeylib538.paper.api.text.PaperMonkeyLibText;

public class PaperPlatformMain implements PlatformMain {
    private static @Nullable GitPackManagerInitializer plugin = null;

    @Override
    public void refreshDatapacks() {
        MinecraftServer.getServer().getPackRepository().reload();
    }

    @Override
    public void sendMessageToAdmins(MonkeyLibText message) {
        for (final OfflinePlayer operator : getPlugin().getServer().getOperators()) {
            if (operator.getPlayer() == null) continue;
            operator.getPlayer().sendMessage(PaperMonkeyLibText.of(message).getText());
        }
    }

    private static void setPlugin(GitPackManagerInitializer plugin) {
        PaperPlatformMain.plugin = plugin;
    }

    public static @Nullable GitPackManagerInitializer getPlugin() {
        return plugin;
    }


    public static final class GitPackManagerInitializer extends JavaPlugin {
        @Override
        public void onEnable() {
            PaperPlatformMain.setPlugin(this);
            GitPackManager.initialize();
        }
    }

}
