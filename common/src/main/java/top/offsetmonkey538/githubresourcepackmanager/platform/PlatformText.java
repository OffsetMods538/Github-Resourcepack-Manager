package top.offsetmonkey538.githubresourcepackmanager.platform;

import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.monkeylib538.api.text.MonkeyLibText;

import static top.offsetmonkey538.githubresourcepackmanager.platform.ServiceLoader.load;

public interface PlatformText {
    PlatformText INSTANCE = load(PlatformText.class);

    void sendUpdateMessage(final MonkeyLibText[] updateMessage, boolean adminsOnly);
}
