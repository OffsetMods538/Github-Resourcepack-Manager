package top.offsetmonkey538.githubresourcepackmanager.platform.fabric;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.ControlFlowAware;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformCommand;
import top.offsetmonkey538.monkeylib538.fabric.api.command.FabricCommandAbstractionApi;

import java.util.Optional;

public class FabricPlatformCommand implements PlatformCommand {
    @Override
    public int executeRequestPackCommand(@NotNull CommandContext<Object> ctx) throws CommandSyntaxException {
        final ServerCommandSource source = FabricCommandAbstractionApi.get(ctx);
        final ServerPlayerEntity player = source.getPlayerOrThrow();
        final MinecraftServer.ServerResourcePackProperties resourcePackProperties = source.getServer().getResourcePackProperties().orElse(null);
        if (resourcePackProperties == null) {
            source.sendFeedback(() -> Text.literal("Failed to send pack update packet to client!"), true);
            return 0;
        }

        player.networkHandler.send(
                new ResourcePackSendS2CPacket(
                        resourcePackProperties.id(),
                        resourcePackProperties.url(),
                        resourcePackProperties.hash(),
                        resourcePackProperties.isRequired(),
                        Optional.ofNullable(resourcePackProperties.prompt())
                ),
                null
        );

        return ControlFlowAware.Command.SINGLE_SUCCESS;
    }
}
