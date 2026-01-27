package top.offsetmonkey538.gitpackmanager.platform;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jspecify.annotations.NonNull;

import static top.offsetmonkey538.gitpackmanager.platform.ServiceLoader.load;

public interface PlatformCommand {
    PlatformCommand INSTANCE = load(PlatformCommand.class);

    int executeRequestPackCommand(final @NonNull CommandContext<Object> ctx) throws CommandSyntaxException;
}
