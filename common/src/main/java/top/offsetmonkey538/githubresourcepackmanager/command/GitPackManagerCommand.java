package top.offsetmonkey538.githubresourcepackmanager.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.githubresourcepackmanager.handler.GitHandler;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformCommand;
import top.offsetmonkey538.monkeylib538.api.command.CommandAbstractionApi;
import top.offsetmonkey538.monkeylib538.api.command.CommandRegistrationApi;
import top.offsetmonkey538.monkeylib538.api.log.MonkeyLibLogger;
import top.offsetmonkey538.monkeylib538.api.text.MonkeyLibStyle;
import top.offsetmonkey538.monkeylib538.api.text.MonkeyLibText;

import java.util.Map;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;
import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.*;
import static top.offsetmonkey538.monkeylib538.api.command.CommandAbstractionApi.literal;
import static top.offsetmonkey538.monkeylib538.api.command.CommandAbstractionApi.sendText;

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
                );
    }

    private static void runTriggerUpdate(CommandContext<Object> context, boolean force) {
        final MonkeyLibLogger.LogListener infoListener = (message, error) -> {
            sendText(context, MonkeyLibText.of(String.format("[%s] %s", MOD_ID, message)).applyStyle(style -> style.withColor(MonkeyLibStyle.Color.GRAY)));
        };
        final MonkeyLibLogger.LogListener warnListener = (message, error) -> {
            final MonkeyLibText text = MonkeyLibText
                    .of("[%s] %s".formatted(MOD_ID, message))
                    .applyStyle(style -> style.withColor(MonkeyLibStyle.Color.YELLOW));

            if (error != null) text.applyStyle(style -> style.withShowText(MonkeyLibText.of(ExceptionUtils.getRootCauseMessage(error))));

            sendText(context, text);
        };
        GithubResourcepackManager.LOGGER.addListener(MonkeyLibLogger.LogLevel.INFO, infoListener);
        GithubResourcepackManager.LOGGER.addListener(MonkeyLibLogger.LogLevel.WARN, warnListener);

        GithubResourcepackManager.updatePack(force ? GithubResourcepackManager.UpdateType.COMMAND_FORCE : GithubResourcepackManager.UpdateType.COMMAND);

        GithubResourcepackManager.LOGGER.removeListener(MonkeyLibLogger.LogLevel.INFO, infoListener);
        GithubResourcepackManager.LOGGER.removeListener(MonkeyLibLogger.LogLevel.WARN, warnListener);
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
