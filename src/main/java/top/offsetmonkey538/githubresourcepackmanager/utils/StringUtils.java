package top.offsetmonkey538.githubresourcepackmanager.utils;

import java.util.Map;

public final class StringUtils {
    private StringUtils() {

    }

    /**
     * Replaces all instances of the keys in the placeholders map with their values.
     *
     * @param string The string to replace placeholders in.
     * @param placeholders The placeholders to replace.
     * @return The original string with all instances of the keys in the placeholders map replaced with their values.
     */
    public static String replacePlaceholders(String string, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            string = string.replace(entry.getKey(), entry.getValue());
        }
        return string;
    }
}
