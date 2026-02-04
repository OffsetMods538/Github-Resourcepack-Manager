package top.offsetmonkey538.gitpackmanager.common.platform;

import net.kyori.adventure.text.Component;

import static top.offsetmonkey538.gitpackmanager.common.platform.ServiceLoader.load;

public interface PlatformMain {
    PlatformMain INSTANCE = load(PlatformMain.class);

    /**
     * Tell the server to refresh the list of available datapacks.
     * <p>
     * This way the user won't have to run {@code /datapack list} before being able to enable them
     */
    void refreshDatapacks();

    /**
     * Reload all enabled datapacks
     */
    void reloadEnabledDatapacks();

    /**
     * Sends provided message to currently online admins
     *
     * @param message the message to send
     */
    void sendMessageToAdmins(final Component message);
}
