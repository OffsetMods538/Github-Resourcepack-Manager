package top.offsetmonkey538.gitpackmanager.modded.platform;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.world.level.storage.LevelResource;
import org.jspecify.annotations.Nullable;
import top.offsetmonkey538.gitpackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.gitpackmanager.modded.mixin.SettingsAccessor;
import top.offsetmonkey538.gitpackmanager.modded.mixin.DedicatedServerAccessor;
import top.offsetmonkey538.gitpackmanager.modded.mixin.DedicatedServerSettingsAccessor;
import top.offsetmonkey538.gitpackmanager.platform.PlatformServerProperties;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class ModdedPlatformServerProperties implements PlatformServerProperties {

    @Override
    public @Nullable String getResourcePackUrl() {
        final Optional<MinecraftServer.ServerResourcePackInfo> resourcePackProperties = ModdedPlatformMain.getServer().getServerResourcePack();

        return resourcePackProperties.map(MinecraftServer.ServerResourcePackInfo::url).orElse(null);
    }

    @Override
    public Path getDatapacksDir() {
        return ModdedPlatformMain.getServer().getWorldPath(LevelResource.DATAPACK_DIR);
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        final DedicatedServerSettings propertiesLoader = ((DedicatedServerAccessor) ModdedPlatformMain.getServer()).getSettings();

        propertiesLoader.update(propertiesHandler -> {
            final Properties serverProperties = ((SettingsAccessor) propertiesHandler).getProperties();

            properties.forEach(serverProperties::setProperty);

            return propertiesHandler;
        });
    }

    @Override
    public void reload() throws GithubResourcepackManagerException {
        final DedicatedServerSettings propertiesLoader = ((DedicatedServerAccessor) ModdedPlatformMain.getServer()).getSettings();
        final DedicatedServerSettingsAccessor propertiesLoaderAccess = (DedicatedServerSettingsAccessor) propertiesLoader;

        propertiesLoaderAccess.setProperties(DedicatedServerProperties.fromFile(propertiesLoaderAccess.getSource()));
    }
}
