package top.offsetmonkey538.gitpackmanager.platform;

import top.offsetmonkey538.monkeylib538.common.api.text.MonkeyLibText;

import static top.offsetmonkey538.gitpackmanager.platform.ServiceLoader.load;

public interface PlatformText {
    PlatformText INSTANCE = load(PlatformText.class);

    void sendUpdateMessage(final MonkeyLibText updateMessage, boolean adminsOnly);
}
