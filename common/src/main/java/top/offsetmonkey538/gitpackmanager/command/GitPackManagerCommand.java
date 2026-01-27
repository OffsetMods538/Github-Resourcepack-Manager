package top.offsetmonkey538.gitpackmanager.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import top.offsetmonkey538.gitpackmanager.GithubResourcepackManager;
import top.offsetmonkey538.gitpackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.gitpackmanager.handler.GitHandler;
import top.offsetmonkey538.gitpackmanager.platform.PlatformCommand;
import top.offsetmonkey538.monkeylib538.common.api.command.CommandAbstractionApi;
import top.offsetmonkey538.monkeylib538.common.api.command.CommandRegistrationApi;
import top.offsetmonkey538.monkeylib538.common.api.text.MonkeyLibStyle;
import top.offsetmonkey538.monkeylib538.common.api.text.MonkeyLibText;
import top.offsetmonkey538.offsetutils538.api.log.OffsetLogger;

import java.util.Map;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;
import static top.offsetmonkey538.gitpackmanager.GithubResourcepackManager.LOGGER;
import static top.offsetmonkey538.gitpackmanager.GithubResourcepackManager.MOD_ID;
import static top.offsetmonkey538.gitpackmanager.GithubResourcepackManager.UpdateType;
import static top.offsetmonkey538.gitpackmanager.GithubResourcepackManager.clearAdminMessageQueue;
import static top.offsetmonkey538.gitpackmanager.GithubResourcepackManager.config;
import static top.offsetmonkey538.gitpackmanager.GithubResourcepackManager.createUpdateMessage;
import static top.offsetmonkey538.gitpackmanager.GithubResourcepackManager.generatePlaceholders;
import static top.offsetmonkey538.gitpackmanager.GithubResourcepackManager.resourcePackHandler;
import static top.offsetmonkey538.monkeylib538.common.api.command.CommandAbstractionApi.literal;
import static top.offsetmonkey538.monkeylib538.common.api.command.CommandAbstractionApi.sendText;

public final class GitPackManagerCommand {

    public static void register() {
        CommandRegistrationApi.registerCommand(createCommand());
    }

    private static LiteralArgumentBuilder<?> createCommand() {
        return literal("gh-rp-manager")
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
            sendText(context, MonkeyLibText.of(String.format("[%s] %s", MOD_ID, message)).applyStyle(style -> style.withColor(MonkeyLibStyle.Color.GRAY)));
        };
        final OffsetLogger.LogListener warnListener = (message, error) -> {
            final MonkeyLibText text = MonkeyLibText
                    .of("[%s] %s".formatted(MOD_ID, message))
                    .applyStyle(style -> style.withColor(MonkeyLibStyle.Color.YELLOW));

            if (error != null) text.applyStyle(style -> style.withShowText(MonkeyLibText.of(ExceptionUtils.getRootCauseMessage(error))));

            sendText(context, text);
        };
        GithubResourcepackManager.LOGGER.addListener(OffsetLogger.LogLevel.INFO, infoListener);
        GithubResourcepackManager.LOGGER.addListener(OffsetLogger.LogLevel.WARN, warnListener);

        GithubResourcepackManager.updatePack(force ? GithubResourcepackManager.UpdateType.COMMAND_FORCE : GithubResourcepackManager.UpdateType.COMMAND, false).thenRun(() -> {
            GithubResourcepackManager.LOGGER.removeListener(OffsetLogger.LogLevel.INFO, infoListener);
            GithubResourcepackManager.LOGGER.removeListener(OffsetLogger.LogLevel.WARN, warnListener);
        });
    }

    private static int runTestUpdateMessage(CommandContext<Object> context, boolean isResource) {
        final GitHandler gitHandler = new GitHandler();
        try {
            gitHandler.updateRepositoryAndGenerateCommitProperties();
        } catch (GithubResourcepackManagerException e) {
            CommandAbstractionApi.sendError(context, "Failed to update repository, git related placeholders will not be replaced!");
            CommandAbstractionApi.sendError(context, "Cause:\n%s\n", e);
            LOGGER.error("Failed to update repository, git related placeholders will not be replaced!", e);
        }
        final Map<String, String> placeholders = generatePlaceholders(gitHandler, isResource ? resourcePackHandler : null, UpdateType.COMMAND, isResource ? "resource" : "data", true);

        final MonkeyLibText[] text;
        try {
            text = createUpdateMessage(isResource ? config.get().resourcePackProvider.updateMessage : config.get().dataPackProvider.updateMessage, placeholders);
        } catch (GithubResourcepackManagerException e) {
            CommandAbstractionApi.sendError(context, "Failed to create update message!");
            CommandAbstractionApi.sendError(context, "Cause:\n%s\n", e);
            LOGGER.error("Failed to create update message for %spack!", e, isResource ? "resource" : "data");
            return 0;
        }

        for (final MonkeyLibText line : text) CommandAbstractionApi.sendText(context, line);
        return 1;
    }
}
