package top.offsetmonkey538.githubresourcepackmanager.platform;

import java.nio.file.Path;

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
}
