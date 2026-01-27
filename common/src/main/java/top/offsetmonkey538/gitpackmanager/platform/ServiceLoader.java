package top.offsetmonkey538.gitpackmanager.platform;

public final class ServiceLoader {
    private ServiceLoader() {

    }

    public static <T> T load(Class<T> clazz) {
        return java.util.ServiceLoader.load(clazz, ServiceLoader.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Failed to load service for " + clazz.getName()));
    }
}
