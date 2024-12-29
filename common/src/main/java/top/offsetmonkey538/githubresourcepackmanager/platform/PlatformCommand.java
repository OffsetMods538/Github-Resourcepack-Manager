package top.offsetmonkey538.githubresourcepackmanager.platform;

import static top.offsetmonkey538.githubresourcepackmanager.platform.ServiceLoader.load;

public interface PlatformCommand {
    PlatformCommand INSTANCE = load(PlatformCommand.class);

    void registerGithubRpManagerCommand();
}
