package top.offsetmonkey538.gitpackmanager.utils;

import top.offsetmonkey538.gitpackmanager.exception.GitPackManagerException;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;

import static top.offsetmonkey538.gitpackmanager.GitPackManager.LOGGER;
import static top.offsetmonkey538.gitpackmanager.GitPackManager.RESOURCEPACK_NAME_PATTERN;

public final class StringUtils {
    private StringUtils() {}

    /**
     * Replaces all instances of the keys in the placeholders map with their values.
     * <p>
     * Replaces {@code '} with {@code \'} in the placeholders if {@code escapeSingleQuotes} is true.
     * <br />
     * Replaces {@code "} with {@code \"} in the placeholders if {@code escapeDoubleQuotes} is true.
     *
     * @param string The string to replace placeholders in.
     * @param placeholders The placeholders to replace.
     * @param escapeSingleQuotes Whether single quotes (') should be escaped.
     * @param escapeDoubleQuotes Whether double quotes (") should be escaped.
     * @return The original string with all instances of the keys in the placeholders map replaced with their values.
     */
    public static String replacePlaceholders(String string, Map<String, String> placeholders, boolean escapeSingleQuotes, boolean escapeDoubleQuotes) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String placeholderValue = entry.getValue();
            if (escapeSingleQuotes) placeholderValue = placeholderValue.replace("'", "\\'");
            if (escapeDoubleQuotes) placeholderValue = placeholderValue.replace("\"", "\\\"");

            string = string.replace(entry.getKey(), placeholderValue);
        }
        return string;
    }

    public static int extractPriorityFromFile(File file) {
        final int result = extractPriorityFromFileInternal(file);
        if (result != -1) return result;
        LOGGER.error("File '%s' doesn't start with priority!", file);
        return -1;
    }

    private static int extractPriorityFromFileInternal(File file) {
        final String filename = file.getName();

        final Matcher matcher = RESOURCEPACK_NAME_PATTERN.matcher(filename);

        if (!matcher.find()) {
            return -1;
        }

        return Integer.parseInt(matcher.group().replace('-', ' ').strip());
    }

    public static String nameWithoutPriorityString(File file) throws GitPackManagerException {
        final String filename = file.getName();

        final Matcher matcher = RESOURCEPACK_NAME_PATTERN.matcher(filename);

        if (!matcher.find()) throw new GitPackManagerException("File '%s' doesn't start with priority!", file);

        return filename.replace(matcher.group(), "").strip();
    }
}
