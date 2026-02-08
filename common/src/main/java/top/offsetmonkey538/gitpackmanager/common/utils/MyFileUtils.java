package top.offsetmonkey538.gitpackmanager.common.utils;

import top.offsetmonkey538.gitpackmanager.common.exception.GitPackManagerException;

import java.io.File;
import java.io.IOException;

public final class MyFileUtils {
    private MyFileUtils() {}

    public static File createDir(File file) throws GitPackManagerException {
        if (!file.exists() && !file.mkdirs()) throw new GitPackManagerException("Failed to create directory '%s'!", file);
        return file;
    }

    public static void createNewFile(File file) throws GitPackManagerException {
        if (file.exists() && !file.delete()) throw new GitPackManagerException("Failed to delete file '%s'!", file);

        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        } catch (IOException e) {
            throw new GitPackManagerException("Failed to create file '%s'!", e, file);
        }
    }
}
