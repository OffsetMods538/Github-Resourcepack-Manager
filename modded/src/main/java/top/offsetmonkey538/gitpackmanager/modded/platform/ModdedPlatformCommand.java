package top.offsetmonkey538.gitpackmanager.modded.platform;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import top.offsetmonkey538.gitpackmanager.platform.PlatformCommand;
import top.offsetmonkey538.monkeylib538.modded.api.command.ModdedCommandAbstractionApi;

import java.util.Optional;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ModdedPlatformCommand implements PlatformCommand {
    @Override
    public int executeRequestPackCommand(CommandContext<Object> ctx) throws CommandSyntaxException {
        final CommandSourceStack source = ModdedCommandAbstractionApi.get(ctx);
        final ServerPlayer player = source.getPlayerOrException();
        final MinecraftServer.ServerResourcePackInfo resourcePackProperties = source.getServer().getServerResourcePack().orElse(null);
        if (resourcePackProperties == null) {
            source.sendSuccess(() -> Component.literal("Failed to send pack update packet to client!"), true);
            return 0;
        }

        player.connection.send(
                new ClientboundResourcePackPushPacket(
                        resourcePackProperties.id(),
                        resourcePackProperties.url(),
                        resourcePackProperties.hash(),
                        resourcePackProperties.isRequired(),
                        Optional.ofNullable(resourcePackProperties.prompt())
                ),
                null
        );

        return SINGLE_SUCCESS;
    }
}
