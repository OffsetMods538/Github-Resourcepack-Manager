package top.offsetmonkey538.gitpackmanager.modded.platform;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import top.offsetmonkey538.gitpackmanager.platform.PlatformText;
import top.offsetmonkey538.monkeylib538.common.api.text.MonkeyLibText;
import top.offsetmonkey538.monkeylib538.modded.api.player.ModdedPlayerApi;
import top.offsetmonkey538.monkeylib538.modded.api.text.ModdedMonkeyLibText;

public class ModdedPlatformText implements PlatformText {
    @Override
    public void sendUpdateMessage(MonkeyLibText updateMessage, boolean adminsOnly) {
        assert ModdedPlatformMain.getServer() != null : "Literally HOW?? Why are we trying to send the update message when the server hasn't started yet!??!?!?";
        final PlayerList playerList = ModdedPlatformMain.getServer().getPlayerList();

        if (!adminsOnly) {
            playerList.broadcastSystemMessage(ModdedMonkeyLibText.of(updateMessage).getText(), false);
            return;
        }

        for (final ServerPlayer player : playerList.getPlayers()) {
            if (!ModdedPlayerApi.isPlayerOp(playerList, player)) continue;
            player.sendSystemMessage(ModdedMonkeyLibText.of(updateMessage).getText(), false);
        }
    }
}
