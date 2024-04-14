package top.offsetmonkey538.githubresourcepackmanager.utils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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
        int length;
        while ((length = fileInputStream.read(bytes)) >= 0) {
            zipOutputStream.write(bytes, 0, length);
        }
        fileInputStream.close();
    }

    public static void unzipFile(File fileToUnzip, File destinationDir) throws IOException {
        final ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(fileToUnzip));

        final byte[] buffer = new byte[1024];
        ZipEntry zipEntry = zipInputStream.getNextEntry();

        while (zipEntry != null) {
            final File newFile = newFileFromZipEntry(destinationDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
                zipEntry = zipInputStream.getNextEntry();
                continue;
            }

            final File parent = newFile.getParentFile();
            if (!parent.isDirectory() && !parent.mkdirs()) {
                throw new IOException("Failed to create directory " + newFile);
            }

            // Write file content
            try (final FileOutputStream fileOutputStream = new FileOutputStream(newFile)) {
                int length;
                while ((length = zipInputStream.read(buffer)) >= 0) {
                    fileOutputStream.write(buffer, 0, length);
                }
            }

            zipEntry = zipInputStream.getNextEntry();
        }

        zipInputStream.closeEntry();
        zipInputStream.close();
    }

    private static File newFileFromZipEntry(File destinationDir, ZipEntry zipEntry) throws IOException {
        final File destinationFile = new File(destinationDir, zipEntry.getName());

        String destinationDirPath = destinationDir.getCanonicalPath();
        String destinationFilePath = destinationFile.getCanonicalPath();

        if (!destinationFilePath.startsWith(destinationDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destinationFile;
    }
}
