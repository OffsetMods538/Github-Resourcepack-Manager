package top.offsetmonkey538.gitpackmanager.common.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import top.offsetmonkey538.gitpackmanager.common.GitPackManager;
import top.offsetmonkey538.gitpackmanager.common.exception.GitPackManagerException;
import top.offsetmonkey538.gitpackmanager.common.handler.GitHandler;
import top.offsetmonkey538.gitpackmanager.common.platform.PlatformCommand;
import top.offsetmonkey538.monkeylib538.common.api.command.CommandAbstractionApi;
import top.offsetmonkey538.monkeylib538.common.api.command.CommandRegistrationApi;
import top.offsetmonkey538.offsetutils538.api.log.OffsetLogger;

import java.util.Map;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;
import static top.offsetmonkey538.gitpackmanager.common.GitPackManager.LOGGER;
import static top.offsetmonkey538.gitpackmanager.common.GitPackManager.MOD_ID;
import static top.offsetmonkey538.gitpackmanager.common.GitPackManager.UpdateType;
import static top.offsetmonkey538.gitpackmanager.common.GitPackManager.clearAdminMessageQueue;
import static top.offsetmonkey538.gitpackmanager.common.GitPackManager.config;
import static top.offsetmonkey538.gitpackmanager.common.GitPackManager.createUpdateMessage;
import static top.offsetmonkey538.gitpackmanager.common.GitPackManager.generatePlaceholders;
import static top.offsetmonkey538.gitpackmanager.common.GitPackManager.resourcePackHandler;
import static top.offsetmonkey538.monkeylib538.common.api.command.CommandAbstractionApi.literal;
import static top.offsetmonkey538.monkeylib538.common.api.command.CommandAbstractionApi.sendText;
import static top.offsetmonkey538.offsetutils538.api.text.ArgReplacer.replaceArgs;

public final class GitPackManagerCommand {

    public static void register() {
        CommandRegistrationApi.registerCommand(createCommand());
    }

    private static LiteralArgumentBuilder<?> createCommand() {
        return literal(MOD_ID)
                .then(literal("request-pack")
                        .requires(CommandAbstractionApi::executedByPlayer)
                        .executes(PlatformCommand.INSTANCE::executeRequestPackCommand)
                )

                .then(literal("trigger-update")
                        .requires(CommandAbstractionApi::isOp)
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

                .then(literal("test-update-message")
                        .requires(CommandAbstractionApi::isOp)
                        .then(literal("resource").executes(context -> runTestUpdateMessage(context, true)))
                        .then(literal("data").executes(context -> runTestUpdateMessage(context, false)))
                )

                .then(literal("reset-admin-message-queue")
                        .requires(CommandAbstractionApi::isOp)
                        .executes(context -> {
                            clearAdminMessageQueue();
                            CommandAbstractionApi.sendMessage(context, "Admin message queue cleared!");
                            return 1;
                        })
                );
    }

    private static void runTriggerUpdate(CommandContext<Object> context, boolean force) {
        final OffsetLogger.LogListener infoListener = (message, error) -> {
            sendText(context, Component.text(replaceArgs("[%s] %s", MOD_ID, message)).style(style -> style.color(NamedTextColor.GRAY)));
        };
        final OffsetLogger.LogListener warnListener = (message, error) -> {
            final Component text = Component
                    .text(replaceArgs("[%s] %s", MOD_ID, message))
                    .style(style -> style.color(NamedTextColor.YELLOW))
                    .style(error == null ? style -> {} : style -> style.hoverEvent(HoverEvent.showText(Component.text(ExceptionUtils.getRootCauseMessage(error)))));

            sendText(context, text);
        };
        GitPackManager.LOGGER.addListener(OffsetLogger.LogLevel.INFO, infoListener);
        GitPackManager.LOGGER.addListener(OffsetLogger.LogLevel.WARN, warnListener);

        GitPackManager.updatePack(force ? GitPackManager.UpdateType.COMMAND_FORCE : GitPackManager.UpdateType.COMMAND, false).thenRun(() -> {
            GitPackManager.LOGGER.removeListener(OffsetLogger.LogLevel.INFO, infoListener);
            GitPackManager.LOGGER.removeListener(OffsetLogger.LogLevel.WARN, warnListener);
        });
    }

    private static int runTestUpdateMessage(CommandContext<Object> context, boolean isResource) {
        final GitHandler gitHandler = new GitHandler();
        try {
            gitHandler.updateRepositoryAndGenerateCommitProperties();
        } catch (GitPackManagerException e) {
            CommandAbstractionApi.sendError(context, "Failed to update repository, git related placeholders will not be replaced!");
            CommandAbstractionApi.sendError(context, "Cause:\n%s\n", e);
            LOGGER.error("Failed to update repository, git related placeholders will not be replaced!", e);
        }
        final Map<String, String> placeholders = generatePlaceholders(gitHandler, isResource ? resourcePackHandler : null, UpdateType.COMMAND, isResource ? "resource" : "data", true);

        final Component[] text;
        try {
            text = createUpdateMessage(isResource ? config.get().resourcePackProvider.updateMessage : config.get().dataPackProvider.updateMessage, placeholders);
        } catch (GitPackManagerException e) {
            CommandAbstractionApi.sendError(context, "Failed to create update message!");
            CommandAbstractionApi.sendError(context, "Cause:\n%s\n", e);
            LOGGER.error("Failed to create update message for %spack!", e, isResource ? "resource" : "data");
            return 0;
        }

        for (final Component line : text) CommandAbstractionApi.sendText(context, line);
        return 1;
    }
}
