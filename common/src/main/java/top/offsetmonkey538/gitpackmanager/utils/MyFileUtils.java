package top.offsetmonkey538.gitpackmanager.utils;

import top.offsetmonkey538.gitpackmanager.exception.GitPackManager;

import java.io.File;
import java.io.IOException;

public final class MyFileUtils {
    private MyFileUtils() {}

    public static File createDir(File file) throws GitPackManager {
        if (!file.exists() && !file.mkdirs()) throw new GitPackManager("Failed to create directory '%s'!", file);
        return file;
    }

    public static void createNewFile(File file) throws GitPackManager {
        if (file.exists() && !file.delete()) throw new GitPackManager("Failed to delete file '%s'!", file);

        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        } catch (IOException e) {
            throw new GitPackManager("Failed to create file '%s'!", e, file);
        }
    }
}
