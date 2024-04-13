package top.offsetmonkey538.githubresourcepackmanager.mixin;

import net.minecraft.server.dedicated.AbstractPropertiesHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractPropertiesHandler.class)
public interface AbstractPropertiesHandlerMixin {

    @Invoker
    String invokeGetString(String key, String fallback);
}
