package top.offsetmonkey538.githubresourcepackmanager.platform;

import java.util.Map;

import static top.offsetmonkey538.githubresourcepackmanager.platform.ServiceLoader.load;

public interface PlatformServerProperties {
    PlatformServerProperties INSTANCE = load(PlatformServerProperties.class);

    String getResourcePackUrl();
    String getServerPort();
    void setProperties(Map<String, String> properties);
    void reload();
}
