package top.offsetmonkey538.gitpackmanager.paper.platform;

import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import top.offsetmonkey538.gitpackmanager.platform.PlatformText;
import top.offsetmonkey538.monkeylib538.common.api.text.MonkeyLibText;

public class PaperPlatformText implements PlatformText {
    @Override
    public void sendUpdateMessage(MonkeyLibText updateMessage, boolean adminsOnly) {
        final PlayerList players = MinecraftServer.getServer().getPlayerList();
        if (!adminsOnly) {
            players.broadcastSystemMessage(Component.empty(), false);
            // TODO: once I implement paper version of monke: players.broadcastSystemMessage(PaperMonkeyLibText.of(updateMessage).getText(), false);
            return;
        }

        for (final ServerPlayer player : players.players) {
            // TODO: GAME PROFILE ISNT CORRETCT: if (!players.isOp(player.getGameProfile())) continue;
            player.sendSystemMessage(Component.empty());
            // TODO: once I implement paper version of monke: player.sendSystemMessage(PaperMonkeyLibText.of(updateMessage).getText());
        }
    }
}
