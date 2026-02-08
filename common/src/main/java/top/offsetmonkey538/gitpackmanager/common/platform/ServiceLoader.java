package top.offsetmonkey538.gitpackmanager.common.platform;

import static top.offsetmonkey538.gitpackmanager.common.GitPackManager.LOGGER;

public final class ServiceLoader {
    private ServiceLoader() {}

    public static <T> T load(Class<T> clazz) {
        LOGGER.info("Loading service for: %s", clazz);
        return java.util.ServiceLoader.load(clazz, ServiceLoader.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Failed to load service for " + clazz.getName()));
    }
}
