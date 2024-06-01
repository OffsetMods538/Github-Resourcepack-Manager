package top.offsetmonkey538.githubresourcepackmanager.handler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.githubresourcepackmanager.utils.MyFileUtils;
import top.offsetmonkey538.githubresourcepackmanager.utils.StringUtils;
import top.offsetmonkey538.githubresourcepackmanager.utils.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.*;

public class PackHandler {
    private Path outputPackPath;

    public void generatePack(boolean wasUpdated, Path oldPackPath, String oldPackName) throws GithubResourcepackManagerException {
        outputPackPath = handleOldPackAndGetOutputPackPath(wasUpdated, oldPackPath, oldPackName);

        // If using the old pack, don't generate a new one.
        if (!wasUpdated) return;

        // Generate new one
        generateNewPack();
    }

    private void generateNewPack() throws GithubResourcepackManagerException {
        final boolean isMultiPack;

        try {
            isMultiPack = getPackType();
        } catch (GithubResourcepackManagerException e) {
            throw new GithubResourcepackManagerException("Failed to determine pack type!", e);
        }


        // Create temp directories to extract pack(s) into.
        final Path tmpDir;
        try {
            tmpDir = Files.createDirectories(RESOURCEPACK_FOLDER.resolve("temp"));
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to create temporary directory!", e);
        }
        final File tempOutputDir = MyFileUtils.createDir(tmpDir.resolve("output").toFile());

        // Extract packs into the temporary packs directory
        final List<File> sourcePacks;
        try {
            sourcePacks = extractSourcePacks(isMultiPack, tempOutputDir);
        } catch (GithubResourcepackManagerException e) {
            throw new GithubResourcepackManagerException("Failed to extract source packs!", e);
        }

        // Write file with source pack names
        try {
            writeSourcePacksFile(isMultiPack, sourcePacks, tempOutputDir);
        } catch (GithubResourcepackManagerException e) {
            throw new GithubResourcepackManagerException("Failed to write pack content file!", e);
        }

        // Zip the pack content and put the output file in the output directory.
        try {
            ZipUtils.zipDirectory(tempOutputDir, outputPackPath.toFile());
        } catch (GithubResourcepackManagerException e) {
            throw new GithubResourcepackManagerException("Failed to zip pack content!", e);
        }

        // Delete temp directories.
        try {
            FileUtils.deleteDirectory(tmpDir.toFile());
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to delete temporary directory!", e);
        }
    }

    private void writeSourcePacksFile(boolean isMultiPack, List<File> sourcePacks, File outputDir) throws GithubResourcepackManagerException {
        final Path sourcePacksFile = outputDir.toPath().resolve("content.txt");
        MyFileUtils.createNewFile(sourcePacksFile.toFile());

        StringBuilder fileContent = new StringBuilder("This pack was put together using GitHub Resourcepack Manager from the following packs:\n\n");

        for (File pack : sourcePacks) {
            fileContent.append("- ").append(isMultiPack ? StringUtils.nameWithoutPriorityString(pack) : pack.getName()).append("\n");
        }

        try {
            Files.writeString(sourcePacksFile, fileContent);
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to write to file '%s'!", e, sourcePacksFile);
        }
    }

    private List<File> extractSourcePacks(boolean isMultiPack, File tempOutputDir) throws GithubResourcepackManagerException {
        // Gather source packs
        final List<File> sourcePacks;

        if (!isMultiPack) sourcePacks = List.of(REPO_ROOT_FOLDER.toFile());
        else sourcePacks = gatherSourcePacks();

        // Extract source packs
        for (File sourcePack : sourcePacks) {
            if (sourcePack.isDirectory()) {
                try {
                    FileUtils.copyDirectory(sourcePack, tempOutputDir, HiddenFileFilter.VISIBLE);
                } catch (IOException e) {
                    throw new GithubResourcepackManagerException("Failed to copy pack '%s' into output directory '%s'!", e, sourcePack, tempOutputDir);
                }
                continue;
            }
            if (sourcePack.getName().endsWith(".zip")) {
                ZipUtils.unzipFile(sourcePack, tempOutputDir);
                continue;
            }

            LOGGER.error("'{}' is not a valid pack! Ignoring...", sourcePack);
            sourcePacks.remove(sourcePack);
        }

        return sourcePacks;
    }

    private List<File> gatherSourcePacks() throws GithubResourcepackManagerException {
        // Gather resource packs
        final File[] sourcePacksArray = PACKS_FOLDER.toFile().listFiles();
        if (sourcePacksArray == null) throw new GithubResourcepackManagerException("Repository contains empty 'packs' folder!");

        // Return source packs sorted in correct order.
        return Stream.of(sourcePacksArray)
                .sorted(Comparator.comparingInt(StringUtils::extractPriorityFromFile).reversed())
                .toList();
    }

    /**
     * Get the type of the pack. Either multi or single pack.
     *
     * @return true if type is multi pack, false if it's single pack.
     * @throws GithubResourcepackManagerException when the type of the pack couldn't be determined.
     */
    private boolean getPackType() throws GithubResourcepackManagerException {
        LOGGER.info("Checking for 'pack.mcmeta' in repository root...");
        final boolean hasPackMcmeta = REPO_ROOT_FOLDER.resolve("pack.mcmeta").toFile().exists();
        LOGGER.info("{}Found!", hasPackMcmeta ? "" : "Not ");

        LOGGER.info("Checking for 'packs' directory in repository root...");
        final boolean hasPacksFolder = PACKS_FOLDER.toFile().exists() && PACKS_FOLDER.toFile().isDirectory();
        LOGGER.info("{}Found!", hasPacksFolder ? "" : "Not ");

        if (hasPackMcmeta && hasPacksFolder) {
            throw new GithubResourcepackManagerException("Found both 'pack.mcmeta' and the 'packs' directory in repository root!");
        }
        if (!hasPackMcmeta && !hasPacksFolder) {
            throw new GithubResourcepackManagerException("Found neither 'pack.mcmeta' nor the 'packs' directory in repository root!");
        }


        if (hasPackMcmeta) {
            LOGGER.info("Using repository root as resource pack.");
            return false;
        }

        LOGGER.info("Using 'packs' directory for resource packs.");
        return true;
    }


    private Path handleOldPackAndGetOutputPackPath(boolean wasUpdated, Path oldPackPath, String oldPackName) {
        if (!wasUpdated) return oldPackPath;

        final String newPackName = generateRandomPackName(oldPackName);

        try {
            if (Files.exists(oldPackPath)) Files.delete(oldPackPath);
        } catch (IOException e) {
            LOGGER.error("Failed to delete old pack!", new GithubResourcepackManagerException("Failed to delete old pack '%s'!", e, oldPackPath));
        }
        return OUTPUT_FOLDER.resolve(newPackName);
    }

    private String generateRandomPackName(String oldPackNameString) {
        long oldPackName = -1;

        try {
            oldPackName = Long.parseLong(oldPackNameString.replace(".zip", ""));
        } catch (NumberFormatException ignored) {}


        long newPackName = Math.abs(new Random().nextLong());
        if (newPackName == oldPackName) newPackName++;
        return newPackName + ".zip";
    }

    public Path getOutputPackPath() {
        return outputPackPath;
    }

    public File getOutputPackFile() {
        return getOutputPackPath().toFile();
    }

    public String getOutputPackName() {
        return getOutputPackFile().getName();
    }
}
