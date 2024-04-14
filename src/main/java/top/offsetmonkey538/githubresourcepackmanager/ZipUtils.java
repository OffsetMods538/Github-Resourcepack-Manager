package top.offsetmonkey538.githubresourcepackmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipUtils {
    private ZipUtils() {

    }

    public static void zipDirectory(File directoryToZip, ZipOutputStream zipOutputStream) throws IOException {
        if (!directoryToZip.isDirectory()) return;

        final File[] children = directoryToZip.listFiles();
        if (children == null) return;
        for (File child : children) {
            zipFile(child, child.getName(), zipOutputStream);
        }
    }

    private static void zipFile(File fileToZip, String filename, ZipOutputStream zipOutputStream) throws IOException {
        if (fileToZip.isHidden()) return;

        if (fileToZip.isDirectory()) {
            filename = filename.endsWith("/") ? filename : filename + "/";

            zipOutputStream.putNextEntry(new ZipEntry(filename));
            zipOutputStream.closeEntry();

            final File[] children = fileToZip.listFiles();
            if (children == null) return;
            for (File child : children) {
                zipFile(child, filename + child.getName(), zipOutputStream);
            }
            return;
        }

        final FileInputStream fileInputStream = new FileInputStream(fileToZip);
        final ZipEntry zipEntry = new ZipEntry(filename);

        zipOutputStream.putNextEntry(zipEntry);

        final byte[] bytes = new byte[1024];
        int lenght;
        while ((lenght = fileInputStream.read(bytes)) >= 0) {
            zipOutputStream.write(bytes, 0, lenght);
        }
        fileInputStream.close();
    }
}
