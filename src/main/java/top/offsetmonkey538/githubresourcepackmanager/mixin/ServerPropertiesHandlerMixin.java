package top.offsetmonkey538.githubresourcepackmanager.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(ServerPropertiesHandler.class)
public interface ServerPropertiesHandlerMixin {
    @Mutable @Accessor void setServerResourcePackProperties(Optional<MinecraftServer.ServerResourcePackProperties> serverResourcePackProperties);
}
