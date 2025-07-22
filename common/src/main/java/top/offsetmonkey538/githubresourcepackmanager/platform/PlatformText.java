package top.offsetmonkey538.githubresourcepackmanager.platform;

import org.jetbrains.annotations.Nullable;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;

import java.util.Map;

import static top.offsetmonkey538.githubresourcepackmanager.platform.ServiceLoader.load;

public interface PlatformText {
    PlatformText INSTANCE = load(PlatformText.class);

    void sendUpdateMessage(final String updateMessage, @Nullable final String updateHoverMessage, Map<String, String> placeholders, boolean adminsOnly) throws GithubResourcepackManagerException;
}
