package top.offsetmonkey538.gitpackmanager.paper.platform;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.minecraft.server.MinecraftServer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import top.offsetmonkey538.gitpackmanager.platform.PlatformCommand;

import java.net.URI;

public class PaperPlatformCommand implements PlatformCommand {
    @SuppressWarnings("UnstableApiUsage")
    @Override
    public int executeRequestPackCommand(@NonNull CommandContext<Object> ctx) throws CommandSyntaxException {
        // TODO: once monke has papier: final CommandSourceStack source = PaperCommandAbstractionApi.get(ctx);
        final CommandSourceStack source = null;
        if (source == null) return 0;
        final Player player = (Player) source.getExecutor();
        if (player == null) return 0;

        final MinecraftServer.ServerResourcePackInfo resourcePackProperties = MinecraftServer.getServer().getServerResourcePack().orElse(null);
        if (resourcePackProperties == null) {
            source.getSender().sendMessage("Failed to send pack update packet to client!");
            return 0;
        }
        player.sendResourcePacks(
                ResourcePackRequest.resourcePackRequest()
                        .packs(
                                ResourcePackInfo.resourcePackInfo(
                                        resourcePackProperties.id(),
                                        URI.create(resourcePackProperties.url()),
                                        resourcePackProperties.hash()
                                )
                        )
                        .replace(true)
                        .required(resourcePackProperties.isRequired())
                        .asResourcePackRequest()
        );
        return Command.SINGLE_SUCCESS;
    }
}
