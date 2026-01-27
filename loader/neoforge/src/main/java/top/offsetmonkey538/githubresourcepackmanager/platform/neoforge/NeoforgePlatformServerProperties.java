package top.offsetmonkey538.githubresourcepackmanager.platform.neoforge;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.world.level.storage.LevelResource;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.githubresourcepackmanager.mixin.SettingsAccessor;
import top.offsetmonkey538.githubresourcepackmanager.mixin.DedicatedServerAccessor;
import top.offsetmonkey538.githubresourcepackmanager.mixin.DedicatedServerSettingsAccessor;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformServerProperties;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class NeoforgePlatformServerProperties implements PlatformServerProperties {

    @Override
    public String getResourcePackUrl() {
        final Optional<MinecraftServer.ServerResourcePackInfo> resourcePackProperties = NeoforgePlatformMain.getServer().getServerResourcePack();

        return resourcePackProperties.map(MinecraftServer.ServerResourcePackInfo::url).orElse(null);
    }

    @Override
    public String getServerPort() {
        return String.valueOf(NeoforgePlatformMain.getServer().getPort());
    }

    @Override
    public Path getDatapacksDir() {
        return NeoforgePlatformMain.getServer().getWorldPath(LevelResource.DATAPACK_DIR);
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        final DedicatedServerSettings propertiesLoader = ((DedicatedServerAccessor) NeoforgePlatformMain.getServer()).getSettings();

        propertiesLoader.update(propertiesHandler -> {
            final Properties serverProperties = ((SettingsAccessor) propertiesHandler).getProperties();

            properties.forEach(serverProperties::setProperty);

            return propertiesHandler;
        });
    }

    @Override
    public void reload() throws GithubResourcepackManagerException {
        final DedicatedServerSettings propertiesLoader = ((DedicatedServerAccessor) NeoforgePlatformMain.getServer()).getSettings();
        final DedicatedServerSettingsAccessor propertiesLoaderAccess = (DedicatedServerSettingsAccessor) propertiesLoader;

        propertiesLoaderAccess.setProperties(DedicatedServerProperties.fromFile(propertiesLoaderAccess.getSource()));
    }
}
