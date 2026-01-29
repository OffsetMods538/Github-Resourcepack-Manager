package top.offsetmonkey538.gitpackmanager.platform;

import com.google.common.hash.Hashing;
import org.jspecify.annotations.Nullable;
import top.offsetmonkey538.gitpackmanager.exception.GitPackManagerException;
import top.offsetmonkey538.gitpackmanager.handler.ResourcePackHandler;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static top.offsetmonkey538.gitpackmanager.GitPackManager.LOGGER;
import static top.offsetmonkey538.gitpackmanager.GitPackManager.RESOURCEPACK_UUID;
import static top.offsetmonkey538.gitpackmanager.GitPackManager.config;
import static top.offsetmonkey538.gitpackmanager.platform.ServiceLoader.load;

public interface PlatformServerProperties {
    PlatformServerProperties INSTANCE = load(PlatformServerProperties.class);

    @Nullable String getResourcePackUrl();
    Path getDatapacksDir();
    void setProperties(Map<String, String> properties);
    void reload() throws GitPackManagerException;

    default void updatePackProperties(ResourcePackHandler packHandler) throws GitPackManagerException {
        final String resourcePackUrl = config.get().getPackUrl(packHandler.getOutputPackName());
        final String resourcePackSha1;
        try {
            // Ignore the fact that sha1 hashing is deprecated as Minecraft uses it for validating server resource packs.
            //noinspection deprecation
            resourcePackSha1 = Hashing.sha1().hashBytes(com.google.common.io.Files.toByteArray(packHandler.getOutputPackFile())).toString();
        } catch (IOException e) {
            throw new GitPackManagerException("Failed to get sha1 hash from pack file '%s'!", e, packHandler.getOutputPackFile());
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
