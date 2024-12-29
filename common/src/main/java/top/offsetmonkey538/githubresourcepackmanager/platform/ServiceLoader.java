package top.offsetmonkey538.githubresourcepackmanager.platform;

public final class ServiceLoader {
    private ServiceLoader() {

    }

    public static <T> T load(Class<T> clazz) {
        return java.util.ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Failed to load service for " + clazz.getName()));
    }
}
