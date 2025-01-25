package top.offsetmonkey538.githubresourcepackmanager.platform.paper;

import org.jetbrains.annotations.Nullable;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformLogging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaperPlatformLogging implements PlatformLogging {

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
        logger.log(Level.FINE, message);
    }

    @Override
    public void info(String message) {
        listeners.get(LogLevel.INFO).forEach(consumer -> consumer.accept(message, null));
        logger.log(Level.INFO, message);
    }

    @Override
    public void warn(String message) {
        listeners.get(LogLevel.WARN).forEach(consumer -> consumer.accept(message, null));
        logger.log(Level.WARNING, message);
    }

    @Override
    public void warn(String message, Throwable error) {
        listeners.get(LogLevel.WARN).forEach(consumer -> consumer.accept(message, error));
        logger.log(Level.WARNING, message, error);
    }

    @Override
    public void error(String message) {
        listeners.get(LogLevel.ERROR).forEach(consumer -> consumer.accept(message, null));
        logger.log(Level.SEVERE, message);
    }

    @Override
    public void error(String message, Throwable error) {
        listeners.get(LogLevel.ERROR).forEach(consumer -> consumer.accept(message, error));
        logger.log(Level.SEVERE, message, error);
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
        PaperPlatformLogging.logger = logger;
    }
}
