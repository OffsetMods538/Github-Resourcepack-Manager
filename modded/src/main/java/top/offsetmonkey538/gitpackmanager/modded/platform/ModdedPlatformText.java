package top.offsetmonkey538.gitpackmanager.modded.platform;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import top.offsetmonkey538.gitpackmanager.platform.PlatformText;
import top.offsetmonkey538.monkeylib538.modded.api.player.ModdedPlayerApi;

public class ModdedPlatformText implements PlatformText {
    @Override
    public void sendUpdateMessage(Component updateMessage, boolean adminsOnly) {
        assert ModdedPlatformMain.getServer() != null : "Literally HOW?? Why are we trying to send the update message when the server hasn't started yet!??!?!?";
        final PlayerList playerList = ModdedPlatformMain.getServer().getPlayerList();

        for (final ServerPlayer player : playerList.getPlayers()) {
            if (adminsOnly && !ModdedPlayerApi.isPlayerOp(playerList, player)) continue;
            ((Audience) player).sendMessage(updateMessage);
        }
    }
}
