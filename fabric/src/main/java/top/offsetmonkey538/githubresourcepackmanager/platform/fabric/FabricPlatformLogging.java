package top.offsetmonkey538.githubresourcepackmanager.platform.fabric;

import org.slf4j.Logger;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformLogging;

public class FabricPlatformLogging implements PlatformLogging {

    private static Logger logger;

    @Override
    public void debug(String message) {
        logger.debug(message);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void warn(String message, Throwable error) {
        logger.warn(message, error);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void error(String message, Throwable error) {
        logger.error(message, error);
    }

    public static void setLogger(Logger logger) {
        FabricPlatformLogging.logger = logger;
    }
}
