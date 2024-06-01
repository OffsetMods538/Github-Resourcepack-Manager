package top.offsetmonkey538.githubresourcepackmanager.utils;

import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.LOGGER;
import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.PACK_NAME_PATTERN;

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

    public static int extractPriorityFromFile(File file) {
        final String filename = file.getName();

        final Matcher matcher = PACK_NAME_PATTERN.matcher(filename);

        if (!matcher.find()) {
            LOGGER.error("File '{}' doesn't start with priority!", file);
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
