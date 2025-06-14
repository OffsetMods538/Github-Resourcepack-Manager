package top.offsetmonkey538.githubresourcepackmanager.platform;

import com.google.common.hash.Hashing;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.githubresourcepackmanager.handler.ResourcePackHandler;

import java.io.IOException;
import java.util.Map;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.*;
import static top.offsetmonkey538.githubresourcepackmanager.platform.PlatformLogging.LOGGER;
import static top.offsetmonkey538.githubresourcepackmanager.platform.ServiceLoader.load;

public interface PlatformServerProperties {
    PlatformServerProperties INSTANCE = load(PlatformServerProperties.class);

    String getResourcePackUrl();
    String getServerPort();
    void setProperties(Map<String, String> properties);
    void reload() throws GithubResourcepackManagerException;

    default void updatePackProperties(ResourcePackHandler packHandler) throws GithubResourcepackManagerException {
        final String resourcePackUrl = config.getPackUrl(packHandler.getOutputPackName());
        final String resourcePackSha1;
        try {
            // Ignore the fact that sha1 hashing is deprecated as Minecraft uses it for validating server resource packs.
            //noinspection deprecation
            resourcePackSha1 = Hashing.sha1().hashBytes(com.google.common.io.Files.toByteArray(packHandler.getOutputPackFile())).toString();
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to get sha1 hash from pack file '%s'!", e, packHandler.getOutputPackFile());
        }

        LOGGER.info("Saving new resource pack properties to 'server.properties' file...");
        LOGGER.info("New resource pack url: '%s'", resourcePackUrl);
        LOGGER.info("New resource pack sha1: '%s'", resourcePackSha1);
        setProperties(Map.of(
                "resource-pack-id", RESOURCEPACK_UUID.toString(),
                "resource-pack", resourcePackUrl,
                "resource-pack-sha1", resourcePackSha1
        ));
        LOGGER.info("New resource pack properties saved!");

        LOGGER.info("Reloading properties from 'server.properties' file...");
        reload();
        LOGGER.info("Properties from 'server.properties' file reloaded!");
    }
}
