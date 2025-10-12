package top.offsetmonkey538.githubresourcepackmanager.platform;

import top.offsetmonkey538.monkeylib538.api.text.MonkeyLibText;

import java.nio.file.Path;
import java.util.List;

import static top.offsetmonkey538.githubresourcepackmanager.platform.ServiceLoader.load;

public interface PlatformMain {
    PlatformMain INSTANCE = load(PlatformMain.class);

    /**
     * Must already contain the mod id.
     * <p>
     * Example: .minecraft/config/github-resourcepack-manager/
     * Example: .minecraft/plugins/Github-Resourcepack-Manager/
     *
     * @return config directory for the mod
     */
    Path getConfigDir();

    /**
     * Must be called when initializing
     *
     * @param work the stuff to run
     */
    void runOnServerStart(Runnable work);

    /**
     * Tell the server to refresh the list of available datapacks.
     * <p>
     * This way the user won't have to run {@code /datapack list} before being able to enable them
     */
    void refreshDatapacks();

    /**
     * Sends provided message to currently online admins
     *
     * @param message the message to send
     */
    void sendMessageToAdmins(final MonkeyLibText message);

    /**
     * Sends messages contained in the provided list to admins when they join
     *
     * @param messageQueue list containing messages to send to admins when they join. Contents can be changed after calling this method.
     */
    void registerSendMessageQueueOnAdminJoin(final List<MonkeyLibText> messageQueue, final MonkeyLibText lastMessage);
}
