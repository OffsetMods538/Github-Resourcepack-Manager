package top.offsetmonkey538.githubresourcepackmanager.mixin;

import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.file.Path;

@Mixin(ServerPropertiesLoader.class)
public interface ServerPropertiesLoaderAccessor {

    @Accessor
    Path getPath();

    @Mutable
    @Accessor
    void setPropertiesHandler(ServerPropertiesHandler handler);
}
