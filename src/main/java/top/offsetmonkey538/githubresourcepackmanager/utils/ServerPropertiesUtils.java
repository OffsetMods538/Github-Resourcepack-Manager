package top.offsetmonkey538.githubresourcepackmanager.utils;

import com.google.common.hash.Hashing;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.githubresourcepackmanager.handler.PackHandler;
import top.offsetmonkey538.githubresourcepackmanager.mixin.AbstractPropertiesHandlerAccessor;
import top.offsetmonkey538.githubresourcepackmanager.mixin.MinecraftDedicatedServerAccessor;
import top.offsetmonkey538.githubresourcepackmanager.mixin.ServerPropertiesLoaderAccessor;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.LOGGER;
import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.config;

public final class ServerPropertiesUtils {
    private ServerPropertiesUtils() {

    }

    public static String getResourcePackUrl(MinecraftServer server) {
        final Optional<MinecraftServer.ServerResourcePackProperties> resourcePackProperties = server.getResourcePackProperties();

        return resourcePackProperties.map(MinecraftServer.ServerResourcePackProperties::url).orElse(null);
    }

    public static void updatePackProperties(MinecraftServer server, PackHandler packHandler) throws GithubResourcepackManagerException {
        final ServerPropertiesLoader propertiesLoader = ((MinecraftDedicatedServerAccessor) server).getPropertiesLoader();

        final String resourcePackUrl = String.format(
                "http://%s:%s/gh-rp-mg/%s",
                config.serverPublicIp,
                propertiesLoader.getPropertiesHandler().serverPort,
                packHandler.getOutputPackName()
        );
        final String resourcePackSha1;
        try {
            // Ignore the fact that sha1 hashing is deprecated as Minecraft uses it for validating server resource packs.
            //noinspection deprecation
            resourcePackSha1 = Hashing.sha1().hashBytes(com.google.common.io.Files.toByteArray(packHandler.getOutputPackFile())).toString();
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to get sha1 hash from pack file '%s'!", e, packHandler.getOutputPackFile());
        }

        LOGGER.info("Saving new resource pack properties to 'server.properties' file...");
        LOGGER.info("New resource pack url: '{}'", resourcePackUrl);
        LOGGER.info("New resource pack sha1: '{}'", resourcePackSha1);
        propertiesLoader.apply(properties -> {
            final Properties serverProperties = ((AbstractPropertiesHandlerAccessor) properties).getProperties();

            serverProperties.setProperty("resource-pack", resourcePackUrl);
            serverProperties.setProperty("resource-pack-sha1", resourcePackSha1);

            return properties;
        });
        LOGGER.info("New resource pack properties saved!");

        LOGGER.info("Reloading properties from 'server.properties' file...");
        final ServerPropertiesLoaderAccessor propertiesLoaderAccess = (ServerPropertiesLoaderAccessor) propertiesLoader;
        propertiesLoaderAccess.setPropertiesHandler(ServerPropertiesHandler.load(propertiesLoaderAccess.getPath()));
        LOGGER.info("Properties from 'server.properties' file reloaded!");
    }
}
