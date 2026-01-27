package top.offsetmonkey538.githubresourcepackmanager.platform.neoforge;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformText;
import top.offsetmonkey538.monkeylib538.api.text.MonkeyLibText;
import top.offsetmonkey538.monkeylib538.neoforge.api.text.NeoforgeMonkeyLibText;

public class NeoforgePlatformText implements PlatformText {
    @Override
    public void sendUpdateMessage(MonkeyLibText updateMessage, boolean adminsOnly) {
        assert NeoforgePlatformMain.getServer() != null : "Literally HOW?? Why are we trying to send the update message when the server hasn't started yet!??!?!?";
        final PlayerList playerManager = NeoforgePlatformMain.getServer().getPlayerList();

        if (!adminsOnly) {
            playerManager.broadcastSystemMessage(NeoforgeMonkeyLibText.of(updateMessage).getText(), false);
            return;
        }

        for (final ServerPlayer player : playerManager.getPlayers()) {
            if (!playerManager.isOp(player.getGameProfile())) continue;
            player.sendSystemMessage(NeoforgeMonkeyLibText.of(updateMessage).getText(), false);
        }
    }
}
