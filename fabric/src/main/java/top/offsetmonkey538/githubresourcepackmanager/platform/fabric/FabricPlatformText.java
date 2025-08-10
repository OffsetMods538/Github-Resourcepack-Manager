package top.offsetmonkey538.githubresourcepackmanager.platform.fabric;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformText;
import top.offsetmonkey538.monkeylib538.api.text.MonkeyLibText;
import top.offsetmonkey538.monkeylib538.fabric.api.text.FabricMonkeyLibText;

public class FabricPlatformText implements PlatformText {
    @Override
    public void sendUpdateMessage(MonkeyLibText updateMessage, boolean adminsOnly) {
        assert FabricPlatformMain.getServer() != null : "Literally HOW?? Why are we trying to send the update message when the server hasn't started yet!??!?!?";
        final PlayerManager playerManager = FabricPlatformMain.getServer().getPlayerManager();
        if (playerManager == null) return;

        if (!adminsOnly) {
            playerManager.broadcast(FabricMonkeyLibText.of(updateMessage).getText(), false);
            return;
        }

        for (final ServerPlayerEntity player : playerManager.getPlayerList()) {
            if (!playerManager.isOperator(player.getGameProfile())) continue;
            player.sendMessageToClient(FabricMonkeyLibText.of(updateMessage).getText(), false);
        }
    }
}
