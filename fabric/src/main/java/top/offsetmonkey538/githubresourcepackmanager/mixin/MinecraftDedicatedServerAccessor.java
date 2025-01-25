package top.offsetmonkey538.githubresourcepackmanager.mixin;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftDedicatedServer.class)
public interface MinecraftDedicatedServerAccessor {

    @Accessor
    ServerPropertiesLoader getPropertiesLoader();
}
