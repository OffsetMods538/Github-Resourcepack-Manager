package top.offsetmonkey538.gitpackmanager.paper.platform;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import top.offsetmonkey538.gitpackmanager.platform.PlatformText;
import top.offsetmonkey538.monkeylib538.common.api.text.MonkeyLibText;
import top.offsetmonkey538.monkeylib538.paper.api.text.PaperMonkeyLibText;

import java.util.Collection;

public class PaperPlatformText implements PlatformText {
    @Override
    public void sendUpdateMessage(MonkeyLibText updateMessage, boolean adminsOnly) {
        final Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        if (!adminsOnly) {
            sendToAll(players, PaperMonkeyLibText.of(updateMessage).getText());
            return;
        }

        for (final Player player : players) {
            if (!player.isOp()) continue;
            player.sendMessage(PaperMonkeyLibText.of(updateMessage).getText());
        }
    }

    private static void sendToAll(Collection<? extends Player> players, Component component) {
        for (Player player : players) player.sendMessage(component);
    }
}
