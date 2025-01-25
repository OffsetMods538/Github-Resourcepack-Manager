package top.offsetmonkey538.githubresourcepackmanager.platform.fabric;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.ControlFlowAware;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformCommand;

import java.util.Optional;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FabricPlatformCommand implements PlatformCommand {
    @Override
    public void registerGithubRpManagerCommand() {
        CommandRegistrationCallback.EVENT.register(FabricPlatformCommand::register);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(literal("gh-rp-manager")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .then(literal("request-pack")
                        .executes(
                                context -> {
                                    final ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                    final MinecraftServer.ServerResourcePackProperties resourcePackProperties = context.getSource().getServer().getResourcePackProperties().orElse(null);
                                    if (resourcePackProperties == null) {
                                        context.getSource().sendFeedback(() -> Text.literal("Failed to send pack update packet to client!"), true);
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
                                })
                )

                .then(literal("trigger-update")
                        .requires(source -> source.hasPermissionLevel(2))
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
        );
    }
}
