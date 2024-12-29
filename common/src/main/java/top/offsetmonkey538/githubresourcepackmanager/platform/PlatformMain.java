package top.offsetmonkey538.githubresourcepackmanager.platform;

import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.function.Function;

import static top.offsetmonkey538.githubresourcepackmanager.platform.ServiceLoader.load;

public interface PlatformMain {
    PlatformMain INSTANCE = load(PlatformMain.class);

    Logger getLogger();
    Path getConfigDir();
    Path getGameDir();

    /**
     * Must be called when initializing
     *
     * @param work the stuff to run
     */
    void runOnServerStart(Runnable work);
}
