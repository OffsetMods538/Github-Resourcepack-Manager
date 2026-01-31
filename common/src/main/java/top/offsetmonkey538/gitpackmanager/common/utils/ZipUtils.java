package top.offsetmonkey538.gitpackmanager.common.utils;

import top.offsetmonkey538.gitpackmanager.common.exception.GitPackManagerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static top.offsetmonkey538.offsetutils538.api.text.ArgReplacer.replaceArgs;

public final class ZipUtils {
    private ZipUtils() {}

    public static void zipDirectory(File directoryToZip, File destinationFile) throws GitPackManagerException {
        if (!directoryToZip.exists())
            throw new GitPackManagerException("Directory '%s' does not exist!", directoryToZip);

        try {
            final FileOutputStream fos = new FileOutputStream(destinationFile);
            final ZipOutputStream zos = new ZipOutputStream(fos);

            zipDirectory(directoryToZip, zos);

            zos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            throw new GitPackManagerException("Failed to find file '%s'!", e, destinationFile);
        } catch (IOException e) {
            throw new GitPackManagerException("Failed to zip directory '%s' to file '%s'!", e, directoryToZip, destinationFile);
        }
    }

    public static void zipDirectory(File directoryToZip, ZipOutputStream zipOutputStream) throws GitPackManagerException {
        if (!directoryToZip.isDirectory()) return;

        final File[] children = directoryToZip.listFiles();
        if (children == null) return;
        for (File child : children) {
            zipFile(child, child.getName(), zipOutputStream);
        }
    }

    private static void zipFile(File fileToZip, String filename, ZipOutputStream zipOutputStream) throws GitPackManagerException {
        if (fileToZip.isHidden()) return;

        if (fileToZip.isDirectory()) {
            filename = filename.endsWith("/") ? filename : filename + "/";

            try {
                final ZipEntry zipEntry = new ZipEntry(filename);

                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.closeEntry();
            } catch (IOException e) {
                throw new GitPackManagerException("Failed to add directory '%s' to zip file!", e, filename);
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
            throw new GitPackManagerException("Failed to add file '%s' to zip file!", e, filename);
        }
    }

    public static void unzipFile(File fileToUnzip, File destinationDir) throws GitPackManagerException {
        final ZipInputStream zipInputStream;
        try {
            zipInputStream = new ZipInputStream(new FileInputStream(fileToUnzip));
        } catch (FileNotFoundException e) {
            throw new GitPackManagerException("Failed to find zip file '%s'!", e, fileToUnzip);
        }

        final byte[] buffer = new byte[1024];
        ZipEntry zipEntry;
        try {
            zipEntry = zipInputStream.getNextEntry();
        } catch (IOException e) {
            throw new GitPackManagerException("Failed to get next entry in zip file '%s'!", e, fileToUnzip);
        }

        while (zipEntry != null) {
            final File newFile;
            try {
                newFile = newFileFromZipEntry(destinationDir, zipEntry);
            } catch (IOException e) {
                throw new GitPackManagerException("Failed to create file from zip entry '%s'!", e, zipEntry);
            }


            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new GitPackManagerException("Failed to create directory '%s'!", newFile);
                }

                try {
                    zipEntry = zipInputStream.getNextEntry();
                } catch (IOException e) {
                    throw new GitPackManagerException("Failed to get next entry in zip file '%'!", e, fileToUnzip);
                }

                continue;
            }

            final File parent = newFile.getParentFile();
            if (!parent.isDirectory() && !parent.mkdirs()) {
                throw new GitPackManagerException("Failed to create directory '%s'", newFile);
            }

            // Write file content
            try (final FileOutputStream fileOutputStream = new FileOutputStream(newFile)) {
                int length;
                while ((length = zipInputStream.read(buffer)) >= 0) {
                    fileOutputStream.write(buffer, 0, length);
                }
            } catch (FileNotFoundException e) {
                throw new GitPackManagerException("Failed to find file!", e);
            } catch (IOException e) {
                throw new GitPackManagerException("Failed to write file content!", e);
            }

            try {
                zipEntry = zipInputStream.getNextEntry();
            } catch (IOException e) {
                throw new GitPackManagerException("Failed to get next entry in zip file '%'!", e, fileToUnzip);
            }
        }

        try {
            zipInputStream.closeEntry();
            zipInputStream.close();
        } catch (IOException e) {
            throw new GitPackManagerException("Failed to close zip file '%s'!", e, fileToUnzip);
        }
    }

    private static File newFileFromZipEntry(File destinationDir, ZipEntry zipEntry) throws IOException {
        final File destinationFile = new File(destinationDir, zipEntry.getName());

        String destinationDirPath = destinationDir.getCanonicalPath();
        String destinationFilePath = destinationFile.getCanonicalPath();

        if (!destinationFilePath.startsWith(destinationDirPath + File.separator)) {
            throw new IOException(replaceArgs("Entry is outside of the target dir '%s'!", zipEntry.getName()));
        }

        return destinationFile;
    }
}
