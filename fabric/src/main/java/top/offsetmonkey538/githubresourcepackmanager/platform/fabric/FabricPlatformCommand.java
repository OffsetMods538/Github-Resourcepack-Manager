package top.offsetmonkey538.githubresourcepackmanager.platform.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.ControlFlowAware;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.exception.ExceptionUtils;
import top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformCommand;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformLogging;

import java.util.Optional;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.MOD_ID;

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
                                    runTriggerUpdate(context, false);
                                    return 1;
                                }
                        )
                        .then(argument("force", bool())
                                .executes(
                                        context -> {
                                            runTriggerUpdate(context, getBool(context, "force"));
                                            return 1;
                                        }
                                )
                        )
                )
        );
    }

    private static void runTriggerUpdate(CommandContext<ServerCommandSource> context, boolean force) {
        final PlatformLogging.LogListener infoListener = (message, error) -> {
            context.getSource().sendMessage(Text.literal(String.format("[%s] %s", MOD_ID, message)));
        };
        final PlatformLogging.LogListener warnListener = (message, error) -> {
            MutableText text = Text
                    .literal(String.format("[%s] %s", MOD_ID, message))
                    .setStyle(Style.EMPTY.withColor(Formatting.YELLOW));

            if (error != null) text.setStyle(Style.EMPTY.withColor(Formatting.YELLOW).withHoverEvent(
                    new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Text.literal(ExceptionUtils.getRootCauseMessage(error))
                    )
            ));

            context.getSource().sendMessage(text);
        };
        PlatformLogging.LOGGER.addListener(PlatformLogging.LogLevel.INFO, infoListener);
        PlatformLogging.LOGGER.addListener(PlatformLogging.LogLevel.WARN, warnListener);

        GithubResourcepackManager.updatePack(force ? GithubResourcepackManager.UpdateType.COMMAND_FORCE : GithubResourcepackManager.UpdateType.COMMAND);

        PlatformLogging.LOGGER.removeListener(PlatformLogging.LogLevel.INFO, infoListener);
        PlatformLogging.LOGGER.removeListener(PlatformLogging.LogLevel.WARN, warnListener);
    }
}
