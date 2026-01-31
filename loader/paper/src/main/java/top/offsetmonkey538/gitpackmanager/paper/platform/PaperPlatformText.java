package top.offsetmonkey538.gitpackmanager.paper.platform;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;
import top.offsetmonkey538.gitpackmanager.platform.PlatformText;

import java.util.stream.Stream;

public class PaperPlatformText implements PlatformText {
    @Override
    public void sendUpdateMessage(Component updateMessage, boolean adminsOnly) {
        Stream<? extends Player> playerStream = Bukkit.getOnlinePlayers().stream();
        if (adminsOnly) playerStream = playerStream.filter(ServerOperator::isOp);

        playerStream.forEach(player -> player.sendMessage(updateMessage));
    }
}
