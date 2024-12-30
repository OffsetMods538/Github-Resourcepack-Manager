package top.offsetmonkey538.githubresourcepackmanager.platform;

import com.google.common.hash.Hashing;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.githubresourcepackmanager.handler.PackHandler;

import java.io.IOException;
import java.util.Map;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.*;
import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.LOGGER;
import static top.offsetmonkey538.githubresourcepackmanager.platform.ServiceLoader.load;

public interface PlatformServerProperties {
    PlatformServerProperties INSTANCE = load(PlatformServerProperties.class);

    String getResourcePackUrl();
    String getServerPort();
    void setProperties(Map<String, String> properties);
    void reload();

    default void updatePackProperties(PackHandler packHandler) throws GithubResourcepackManagerException {
        final String resourcePackUrl = String.format(
                "http://%s:%s/"+ MOD_URI + "/%s",
                config.serverPublicIp,
                getServerPort(),
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
        setProperties(Map.of(
                "resource-pack-id", PACK_UUID.toString(),
                "resource-pack", resourcePackUrl,
                "resource-pack-sha1", resourcePackSha1
        ));
        LOGGER.info("New resource pack properties saved!");

        LOGGER.info("Reloading properties from 'server.properties' file...");
        reload();
        LOGGER.info("Properties from 'server.properties' file reloaded!");
    }
}
