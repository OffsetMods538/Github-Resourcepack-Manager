package top.offsetmonkey538.githubresourcepackmanager.platform.paper;

import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformLogging;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PaperPlatformLogging implements PlatformLogging {

    private static Logger logger;

    @Override
    public void debug(String message) {
        logger.log(Level.FINE, message);
    }

    @Override
    public void info(String message) {
        logger.log(Level.INFO, message);
    }

    @Override
    public void warn(String message) {
        logger.log(Level.WARNING, message);
    }

    @Override
    public void warn(String message, Throwable error) {
        logger.log(Level.WARNING, message, error);
    }

    @Override
    public void error(String message) {
        logger.log(Level.SEVERE, message);
    }

    @Override
    public void error(String message, Throwable error) {
        logger.log(Level.SEVERE, message, error);
    }

    public static void setLogger(Logger logger) {
        PaperPlatformLogging.logger = logger;
    }
}
