package top.offsetmonkey538.githubresourcepackmanager.utils;

import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.PACK_NAME_PATTERN;
import static top.offsetmonkey538.githubresourcepackmanager.platform.PlatformLogging.LOGGER;

public final class StringUtils {
    private StringUtils() {

    }

    /**
     * Replaces all instances of the keys in the placeholders map with their values.
     * <p>
     * Same as calling {@link StringUtils#replacePlaceholders(String, Map, boolean)} with {@code escapeQuotes} false.
     *
     * @param string The string to replace placeholders in.
     * @param placeholders The placeholders to replace.
     * @return The original string with all instances of the keys in the placeholders map replaced with their values.
     */
    public static String replacePlaceholders(String string, Map<String, String> placeholders) {
        return replacePlaceholders(string, placeholders, false);
    }

    /**
     * Replaces all instances of the keys in the placeholders map with their values.
     * <p>
     * Replaces {@code "} with {@code \"} in the placeholders if {@code escapeQuotes} is true.
     *
     * @param string The string to replace placeholders in.
     * @param placeholders The placeholders to replace.
     * @param escapeQuotes Whether quotes should be escaped.
     * @return The original string with all instances of the keys in the placeholders map replaced with their values.
     */
    public static String replacePlaceholders(String string, Map<String, String> placeholders, boolean escapeQuotes) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            // I love strings. "\"" matches " and "\\\"" matches \"
            string = string.replace(entry.getKey(), escapeQuotes ? entry.getValue().replace("\"", "\\\"") : entry.getValue());
        }
        return string;
    }

    public static int extractPriorityFromFile(File file) {
        final String filename = file.getName();

        final Matcher matcher = PACK_NAME_PATTERN.matcher(filename);

        if (!matcher.find()) {
            LOGGER.error("File '%s' doesn't start with priority!", file);
            return -1;
        }

        return Integer.parseInt(matcher.group().replace('-', ' ').strip());
    }

    public static String nameWithoutPriorityString(File file) throws GithubResourcepackManagerException {
        final String filename = file.getName();

        final Matcher matcher = PACK_NAME_PATTERN.matcher(filename);

        if (!matcher.find()) throw new GithubResourcepackManagerException("File '%s' doesn't start with priority!", file);

        return filename.replace(matcher.group(), "").strip();
    }
}
