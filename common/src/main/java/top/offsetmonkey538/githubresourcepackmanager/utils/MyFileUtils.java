package top.offsetmonkey538.githubresourcepackmanager.utils;

import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;

import java.io.File;
import java.io.IOException;

public final class MyFileUtils {
    private MyFileUtils() {

    }

    public static File createDir(File file) throws GithubResourcepackManagerException {
        if (!file.exists() && !file.mkdirs()) throw new GithubResourcepackManagerException("Failed to create directory '%s'!", file);
        return file;
    }

    public static void createNewFile(File file) throws GithubResourcepackManagerException {
        if (file.exists() && !file.delete()) throw new GithubResourcepackManagerException("Failed to delete file '%s'!", file);

        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to create file '%s'!", e, file);
        }
    }
}
