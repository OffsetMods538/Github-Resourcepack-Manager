package top.offsetmonkey538.gitpackmanager.paper.platform;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.world.level.storage.LevelResource;
import org.jspecify.annotations.Nullable;
import top.offsetmonkey538.gitpackmanager.exception.GitPackManagerException;
import top.offsetmonkey538.gitpackmanager.platform.PlatformServerProperties;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class PaperPlatformServerProperties implements PlatformServerProperties {
    @Override
    public @Nullable String getResourcePackUrl() {
        final Optional<MinecraftServer.ServerResourcePackInfo> resourcePackProperties = MinecraftServer.getServer().getServerResourcePack();

        return resourcePackProperties.map(MinecraftServer.ServerResourcePackInfo::url).orElse(null);
    }

    @Override
    public Path getDatapacksDir() {
        return MinecraftServer.getServer().getWorldPath(LevelResource.DATAPACK_DIR);
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        final DedicatedServerSettings settings = ((DedicatedServer) MinecraftServer.getServer()).settings;

        settings.update(dedicatedServerProperties -> {
            properties.forEach(dedicatedServerProperties.properties::setProperty);

            return dedicatedServerProperties;
        });
    }

    @Override
    public void reload() throws GitPackManagerException {
        final DedicatedServerSettings settings = ((DedicatedServer) MinecraftServer.getServer()).settings;

        try {
            final Field sourceField = DedicatedServerSettings.class.getDeclaredField("source");
            final Field propertiesField = DedicatedServerSettings.class.getDeclaredField("properties");

            sourceField.setAccessible(true);
            propertiesField.setAccessible(true);

            propertiesField.set(
                    settings,
                    DedicatedServerProperties.fromFile((Path) sourceField.get(settings), MinecraftServer.getServer().options)
            );
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new GitPackManagerException("Failed to reload 'server.properties' file!", e);
        }
    }
}
