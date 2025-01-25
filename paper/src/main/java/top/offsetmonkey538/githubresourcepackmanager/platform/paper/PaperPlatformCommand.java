package top.offsetmonkey538.githubresourcepackmanager.platform.paper;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.minecraft.server.MinecraftServer;
import org.bukkit.entity.Player;
import top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformCommand;

import java.net.URI;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class PaperPlatformCommand implements PlatformCommand {
    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void registerGithubRpManagerCommand() {
        PaperPlatformMain.getPlugin().getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event. registrar();

            commands.register(literal("gh-rp-manager")
                    .requires(commandSourceStack -> commandSourceStack.getExecutor() instanceof Player)
                    .then(literal("request-pack").executes(
                            context -> {
                                final Player player = (Player) context.getSource().getExecutor();
                                if (player == null) return 0;

                                final MinecraftServer.ServerResourcePackInfo resourcePackProperties = MinecraftServer.getServer().getServerResourcePack().orElse(null);
                                if (resourcePackProperties == null) {
                                    context.getSource().getSender().sendMessage("Failed to send pack update packet to client!");
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
                            })
                    )
                    .then(literal("trigger-update")
                            .requires(source -> source.getSender().isOp())
                            .executes(
                                    context -> {
                                        GithubResourcepackManager.updatePack(GithubResourcepackManager.UpdateType.COMMAND);
                                        return 1;
                                    }
                            )
                            .then(argument("force", bool())
                                    .executes(
                                            context -> {
                                                GithubResourcepackManager.updatePack(GithubResourcepackManager.UpdateType.COMMAND_FORCE);
                                                return 1;
                                            }
                                    )
                            )
                    )
                    .build()
            );
        });
    }

    //@Override
    //public void registerGithubRpManagerCommand() {
    //    CommandRegistrationCallback.EVENT.register(FabricPlatformCommand::register);
    //}

    //public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
    //    dispatcher.register(literal("gh-rp-manager")
    //            .requires(ServerCommandSource::isExecutedByPlayer)
    //            .then(literal("request-pack").executes(
    //                    context -> {
    //                        final ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
    //                        final MinecraftServer.ServerResourcePackProperties resourcePackProperties = context.getSource().getServer().getResourcePackProperties().orElse(null);
    //                        if (resourcePackProperties == null) {
    //                            context.getSource().sendFeedback(() -> Text.literal("Failed to send pack update packet to client!"), true);
    //                            return 0;
    //                        }

    //                        player.networkHandler.send(
    //                                new ResourcePackSendS2CPacket(
    //                                        resourcePackProperties.id(),
    //                                        resourcePackProperties.url(),
    //                                        resourcePackProperties.hash(),
    //                                        resourcePackProperties.isRequired(),
    //                                        Optional.ofNullable(resourcePackProperties.prompt())
    //                                ),
    //                                null
    //                        );

    //                        return ControlFlowAware.Command.SINGLE_SUCCESS;
    //                    })
    //            )
    //    );
    //}
}
