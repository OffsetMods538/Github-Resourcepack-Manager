package top.offsetmonkey538.githubresourcepackmanager.platform.paper;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.entity.Player;
import top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformCommand;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformLogging;

import java.net.URI;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;
import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.MOD_ID;

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
                    .build()
            );
        });
    }

    @SuppressWarnings("UnstableApiUsage")
    private void runTriggerUpdate(CommandContext<CommandSourceStack> context, boolean force) {
        final PlatformLogging.LogListener infoListener = (message, error) -> {
            context.getSource().getSender().sendMessage(Component.text(String.format("[%s] %s", MOD_ID, message)));
        };
        final PlatformLogging.LogListener warnListener = (message, error) -> {
            Component text = Component
                    .text(String.format("[%s] %s", MOD_ID, message))
                    .color(NamedTextColor.YELLOW);

            if (error != null) text = text.hoverEvent(
                    HoverEvent.showText(
                            Component.text(ExceptionUtils.getRootCauseMessage(error))
                    )
            );

            context.getSource().getSender().sendMessage(text);
        };
        PlatformLogging.LOGGER.addListener(PlatformLogging.LogLevel.INFO, infoListener);
        PlatformLogging.LOGGER.addListener(PlatformLogging.LogLevel.WARN, warnListener);

        GithubResourcepackManager.updatePack(force ? GithubResourcepackManager.UpdateType.COMMAND_FORCE : GithubResourcepackManager.UpdateType.COMMAND);

        PlatformLogging.LOGGER.removeListener(PlatformLogging.LogLevel.INFO, infoListener);
        PlatformLogging.LOGGER.removeListener(PlatformLogging.LogLevel.WARN, warnListener);
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
