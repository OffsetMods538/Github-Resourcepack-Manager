package top.offsetmonkey538.githubresourcepackmanager.platform.neoforge;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformCommand;
import top.offsetmonkey538.monkeylib538.neoforge.api.command.NeoforgeCommandAbstractionApi;

import java.util.Optional;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class NeoforgePlatformCommand implements PlatformCommand {
    @Override
    public int executeRequestPackCommand(@NotNull CommandContext<Object> ctx) throws CommandSyntaxException {
        final CommandSourceStack source = NeoforgeCommandAbstractionApi.get(ctx);
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
