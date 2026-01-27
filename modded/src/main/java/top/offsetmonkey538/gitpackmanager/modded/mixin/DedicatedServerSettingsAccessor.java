package top.offsetmonkey538.gitpackmanager.modded.mixin;

import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.file.Path;

@Mixin(DedicatedServerSettings.class)
public interface DedicatedServerSettingsAccessor {

    @Accessor
    Path getSource();

    @Mutable
    @Accessor
    void setProperties(DedicatedServerProperties properties);
}
