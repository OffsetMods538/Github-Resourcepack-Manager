package top.offsetmonkey538.githubresourcepackmanager.platform;

import static top.offsetmonkey538.githubresourcepackmanager.platform.ServiceLoader.load;

public interface PlatformLogging {
    PlatformLogging LOGGER = load(PlatformLogging.class);

    default void debug(String message, Object... args) {
        debug(String.format(message, args));
    }
    default void info(String message, Object... args) {
        info(String.format(message, args));
    }
    default void warn(String message, Object... args) {
        warn(String.format(message, args));
    }
    default void warn(String message, Throwable error, Object... args) {
        warn(String.format(message, args), error);
    }
    default void error(String message, Object... args) {
        error(String.format(message, args));
    }
    default void error(String message, Throwable error, Object... args) {
        error(String.format(message, args), error);
    }

    void debug(String message);
    void info(String message);
    void warn(String message);
    void warn(String message, Throwable error);
    void error(String message);
    void error(String message, Throwable error);
}
