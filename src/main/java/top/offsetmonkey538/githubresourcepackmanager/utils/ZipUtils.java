package top.offsetmonkey538.githubresourcepackmanager.utils;

import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class ZipUtils {
    private ZipUtils() {

    }

    public static void zipDirectory(File directoryToZip, ZipOutputStream zipOutputStream) throws GithubResourcepackManagerException {
        if (!directoryToZip.isDirectory()) return;

        final File[] children = directoryToZip.listFiles();
        if (children == null) return;
        for (File child : children) {
            zipFile(child, child.getName(), zipOutputStream);
        }
    }

    private static void zipFile(File fileToZip, String filename, ZipOutputStream zipOutputStream) throws GithubResourcepackManagerException {
        if (fileToZip.isHidden()) return;

        if (fileToZip.isDirectory()) {
            filename = filename.endsWith("/") ? filename : filename + "/";

            try {
                final ZipEntry zipEntry = new ZipEntry(filename);

                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.closeEntry();
            } catch (IOException e) {
                throw new GithubResourcepackManagerException("Failed to add directory '%s' to zip file!", e, filename);
            }

            final File[] children = fileToZip.listFiles();
            if (children == null) return;
            for (File child : children) {
                zipFile(child, filename + child.getName(), zipOutputStream);
            }
            return;
        }

        try (final FileInputStream fileInputStream = new FileInputStream(fileToZip)) {
            final ZipEntry zipEntry = new ZipEntry(filename);

            zipOutputStream.putNextEntry(zipEntry);

            final byte[] bytes = new byte[1024];
            int length;
            while ((length = fileInputStream.read(bytes)) >= 0) {
                zipOutputStream.write(bytes, 0, length);
            }
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to add file '%s' to zip file!", e, filename);
        }
    }

    public static void unzipFile(File fileToUnzip, File destinationDir) throws GithubResourcepackManagerException {
        final ZipInputStream zipInputStream;
        try {
            zipInputStream = new ZipInputStream(new FileInputStream(fileToUnzip));
        } catch (FileNotFoundException e) {
            throw new GithubResourcepackManagerException("Failed to find zip file '%s'!", e, fileToUnzip);
        }

        final byte[] buffer = new byte[1024];
        ZipEntry zipEntry;
        try {
            zipEntry = zipInputStream.getNextEntry();
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to get next entry in zip file '%'!", e, fileToUnzip);
        }

        while (zipEntry != null) {
            final File newFile;
            try {
                newFile = newFileFromZipEntry(destinationDir, zipEntry);
            } catch (IOException e) {
                throw new GithubResourcepackManagerException("Failed to create file from zip entry '%s'!", e, zipEntry);
            }


            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new GithubResourcepackManagerException("Failed to create directory '%s'!", newFile);
                }

                try {
                    zipEntry = zipInputStream.getNextEntry();
                } catch (IOException e) {
                    throw new GithubResourcepackManagerException("Failed to get next entry in zip file '%'!", e, fileToUnzip);
                }

                continue;
            }

            final File parent = newFile.getParentFile();
            if (!parent.isDirectory() && !parent.mkdirs()) {
                throw new GithubResourcepackManagerException("Failed to create directory '%s'", newFile);
            }

            // Write file content
            try (final FileOutputStream fileOutputStream = new FileOutputStream(newFile)) {
                int length;
                while ((length = zipInputStream.read(buffer)) >= 0) {
                    fileOutputStream.write(buffer, 0, length);
                }
            } catch (FileNotFoundException e) {
                throw new GithubResourcepackManagerException("Failed to find file!", e);
            } catch (IOException e) {
                throw new GithubResourcepackManagerException("Failed to write file content!", e);
            }

            try {
                zipEntry = zipInputStream.getNextEntry();
            } catch (IOException e) {
                throw new GithubResourcepackManagerException("Failed to get next entry in zip file '%'!", e, fileToUnzip);
            }
        }

        try {
            zipInputStream.closeEntry();
            zipInputStream.close();
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to close zip file '%s'!", e, fileToUnzip);
        }
    }

    private static File newFileFromZipEntry(File destinationDir, ZipEntry zipEntry) throws IOException {
        final File destinationFile = new File(destinationDir, zipEntry.getName());

        String destinationDirPath = destinationDir.getCanonicalPath();
        String destinationFilePath = destinationFile.getCanonicalPath();

        if (!destinationFilePath.startsWith(destinationDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir '" + zipEntry.getName() + "'!");
        }

        return destinationFile;
    }
}
