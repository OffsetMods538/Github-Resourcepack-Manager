package top.offsetmonkey538.gitpackmanager.common.platform;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import static top.offsetmonkey538.gitpackmanager.common.platform.ServiceLoader.load;

public interface PlatformCommand {
    PlatformCommand INSTANCE = load(PlatformCommand.class);

    int executeRequestPackCommand(final CommandContext<Object> ctx) throws CommandSyntaxException;
}
