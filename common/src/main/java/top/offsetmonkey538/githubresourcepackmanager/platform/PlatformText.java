package top.offsetmonkey538.githubresourcepackmanager.platform;

import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;

import java.util.Map;

import static top.offsetmonkey538.githubresourcepackmanager.platform.ServiceLoader.load;

public interface PlatformText {
    PlatformText INSTANCE = load(PlatformText.class);

    void sendUpdateMessage(Map<String, String> placeholders) throws GithubResourcepackManagerException;
}
