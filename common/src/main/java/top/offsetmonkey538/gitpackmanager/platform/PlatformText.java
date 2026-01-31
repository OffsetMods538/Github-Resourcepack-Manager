package top.offsetmonkey538.gitpackmanager.platform;

import net.kyori.adventure.text.Component;

import static top.offsetmonkey538.gitpackmanager.platform.ServiceLoader.load;

public interface PlatformText {
    PlatformText INSTANCE = load(PlatformText.class);

    void sendUpdateMessage(final Component updateMessage, boolean adminsOnly);
}
