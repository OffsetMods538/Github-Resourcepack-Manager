package top.offsetmonkey538.githubresourcepackmanager.platform.paper;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformServerProperties;
import xyz.jpenilla.reflectionremapper.ReflectionRemapper;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class PaperPlatformServerProperties implements PlatformServerProperties {
    @Override
    public String getResourcePackUrl() {
        final Optional<MinecraftServer.ServerResourcePackInfo> resourcePackProperties = MinecraftServer.getServer().getServerResourcePack();

        return resourcePackProperties.map(MinecraftServer.ServerResourcePackInfo::url).orElse(null);
    }

    @Override
    public String getServerPort() {
        return String.valueOf(MinecraftServer.getServer().getPort());
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
    public void reload() throws GithubResourcepackManagerException {
        final DedicatedServerSettings settings = ((DedicatedServer) MinecraftServer.getServer()).settings;

        final ReflectionRemapper reflectionRemapper = ReflectionRemapper.forReobfMappingsInPaperJar();


        try {
            final Field sourceField = DedicatedServerSettings.class.getDeclaredField(reflectionRemapper.remapFieldName(DedicatedServerSettings.class, "source"));
            final Field propertiesField = DedicatedServerSettings.class.getDeclaredField(reflectionRemapper.remapFieldName(DedicatedServerSettings.class, "properties"));

            sourceField.setAccessible(true);
            propertiesField.setAccessible(true);

            propertiesField.set(
                    settings,
                    DedicatedServerProperties.fromFile((Path) sourceField.get(settings), MinecraftServer.getServer().options)
            );
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new GithubResourcepackManagerException("Failed to reload 'server.properties' file!", e);
        }
    }
}
