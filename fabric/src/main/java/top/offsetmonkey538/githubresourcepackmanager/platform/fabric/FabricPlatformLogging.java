package top.offsetmonkey538.githubresourcepackmanager.platform.fabric;

import org.slf4j.Logger;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformLogging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FabricPlatformLogging implements PlatformLogging {

    private static Logger logger;
    private static final Map<LogLevel, List<LogListener>> listeners = new HashMap<>(Map.of(
            LogLevel.DEBUG, new ArrayList<>(),
            LogLevel.INFO, new ArrayList<>(),
            LogLevel.WARN, new ArrayList<>(),
            LogLevel.ERROR, new ArrayList<>()
    ));

    @Override
    public void debug(String message) {
        listeners.get(LogLevel.DEBUG).forEach(consumer -> consumer.accept(message, null));
        logger.debug(message);
    }

    @Override
    public void info(String message) {
        listeners.get(LogLevel.INFO).forEach(consumer -> consumer.accept(message, null));
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        listeners.get(LogLevel.WARN).forEach(consumer -> consumer.accept(message, null));
        logger.warn(message);
    }

    @Override
    public void warn(String message, Throwable error) {
        listeners.get(LogLevel.WARN).forEach(consumer -> consumer.accept(message, error));
        logger.warn(message, error);
    }

    @Override
    public void error(String message) {
        listeners.get(LogLevel.ERROR).forEach(consumer -> consumer.accept(message, null));
        logger.error(message);
    }

    @Override
    public void error(String message, Throwable error) {
        listeners.get(LogLevel.ERROR).forEach(consumer -> consumer.accept(message, error));
        logger.error(message, error);
    }

    @Override
    public void addListener(LogLevel level, LogListener listener) {
        listeners.get(level).add(listener);
    }

    @Override
    public void removeListener(LogLevel level, LogListener listener) {
        listeners.get(level).remove(listener);
    }

    public static void setLogger(Logger logger) {
        FabricPlatformLogging.logger = logger;
    }
}
