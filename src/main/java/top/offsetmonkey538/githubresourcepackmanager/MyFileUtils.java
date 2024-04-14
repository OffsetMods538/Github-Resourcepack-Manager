package top.offsetmonkey538.githubresourcepackmanager;

import java.io.File;
import java.io.IOException;

public final class MyFileUtils {
    private MyFileUtils() {

    }

    public static File createDir(File file) {
        if (!file.exists() && !file.mkdirs()) throw new RuntimeException("Couldn't create directory '" + file + "'!");
        return file;
    }

    public static void createNewFile(File file) throws IOException {
        if (file.exists() && !file.delete()) throw new RuntimeException("Couldn't delete file '" + file + "'!");

        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();
    }
}
