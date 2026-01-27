package top.offsetmonkey538.githubresourcepackmanager.platform.fabric;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.util.WorldSavePath;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.githubresourcepackmanager.mixin.AbstractPropertiesHandlerAccessor;
import top.offsetmonkey538.githubresourcepackmanager.mixin.MinecraftDedicatedServerAccessor;
import top.offsetmonkey538.githubresourcepackmanager.mixin.ServerPropertiesLoaderAccessor;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformServerProperties;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class FabricPlatformServerProperties implements PlatformServerProperties {

    @Override
    public String getResourcePackUrl() {
        final Optional<MinecraftServer.ServerResourcePackProperties> resourcePackProperties = FabricPlatformMain.getServer().getResourcePackProperties();

        return resourcePackProperties.map(MinecraftServer.ServerResourcePackProperties::url).orElse(null);
    }

    @Override
    public String getServerPort() {
        return String.valueOf(FabricPlatformMain.getServer().getServerPort());
    }

    @Override
    public Path getDatapacksDir() {
        return FabricPlatformMain.getServer().getSavePath(WorldSavePath.DATAPACKS);
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        final ServerPropertiesLoader propertiesLoader = ((MinecraftDedicatedServerAccessor) FabricPlatformMain.getServer()).getPropertiesLoader();

        propertiesLoader.apply(propertiesHandler -> {
            final Properties serverProperties = ((AbstractPropertiesHandlerAccessor) propertiesHandler).getProperties();

            properties.forEach(serverProperties::setProperty);

            return propertiesHandler;
        });
    }

    @Override
    public void reload() throws GithubResourcepackManagerException {
        final ServerPropertiesLoader propertiesLoader = ((MinecraftDedicatedServerAccessor) FabricPlatformMain.getServer()).getPropertiesLoader();
        final ServerPropertiesLoaderAccessor propertiesLoaderAccess = (ServerPropertiesLoaderAccessor) propertiesLoader;

        propertiesLoaderAccess.setPropertiesHandler(ServerPropertiesHandler.load(propertiesLoaderAccess.getPath()));
    }
}
