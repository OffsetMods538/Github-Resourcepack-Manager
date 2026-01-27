package top.offsetmonkey538.gitpackmanager.modded.platform;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import top.offsetmonkey538.gitpackmanager.platform.PlatformText;
import top.offsetmonkey538.monkeylib538.common.api.text.MonkeyLibText;
import top.offsetmonkey538.monkeylib538.modded.api.text.ModdedMonkeyLibText;

public class ModdedPlatformText implements PlatformText {
    @Override
    public void sendUpdateMessage(MonkeyLibText updateMessage, boolean adminsOnly) {
        assert ModdedPlatformMain.getServer() != null : "Literally HOW?? Why are we trying to send the update message when the server hasn't started yet!??!?!?";
        final PlayerList playerManager = ModdedPlatformMain.getServer().getPlayerList();

        if (!adminsOnly) {
            playerManager.broadcastSystemMessage(ModdedMonkeyLibText.of(updateMessage).getText(), false);
            return;
        }

        for (final ServerPlayer player : playerManager.getPlayers()) {
            // TODO: GAME PROFILE ISNT CORRETCT: if (!playerManager.isOp(player.getGameProfile())) continue;
            player.sendSystemMessage(ModdedMonkeyLibText.of(updateMessage).getText(), false);
        }
    }
}
