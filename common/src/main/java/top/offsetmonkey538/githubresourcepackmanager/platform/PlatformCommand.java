package top.offsetmonkey538.githubresourcepackmanager.platform;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jetbrains.annotations.NotNull;

import static top.offsetmonkey538.githubresourcepackmanager.platform.ServiceLoader.load;

public interface PlatformCommand {
    PlatformCommand INSTANCE = load(PlatformCommand.class);

    int executeRequestPackCommand(final @NotNull CommandContext<Object> ctx) throws CommandSyntaxException;
}
