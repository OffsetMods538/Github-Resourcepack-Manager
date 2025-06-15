package top.offsetmonkey538.githubresourcepackmanager.handler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.jetbrains.annotations.Nullable;
import top.offsetmonkey538.githubresourcepackmanager.config.ModConfig;
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
import static top.offsetmonkey538.githubresourcepackmanager.platform.PlatformLogging.LOGGER;

public class ResourcePackHandler {
    private Path outputPackPath;

    public void generatePack(boolean wasUpdated, Path oldPackPath, String oldPackName) throws GithubResourcepackManagerException {
        outputPackPath = handleOldPackAndGetOutputPackPath(wasUpdated, oldPackPath, oldPackName);

        // If using the old pack, don't generate a new one.
        if (!wasUpdated) return;

        // Generate new one
        generateNewPack();
    }

    private void generateNewPack() throws GithubResourcepackManagerException {
        final List<File> sourcePacks;

        try {
            sourcePacks = gatherSourcePacks();
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
        try {
            extractSourcePacks(sourcePacks, tempOutputDir);
        } catch (GithubResourcepackManagerException e) {
            throw new GithubResourcepackManagerException("Failed to extract source packs!", e);
        }

        // Write file with source pack names
        try {
            writeSourcePacksFile(sourcePacks, tempOutputDir);
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

    private void writeSourcePacksFile(List<File> sourcePacks, File outputDir) throws GithubResourcepackManagerException {
        if (sourcePacks.size() == 1) return;

        final Path sourcePacksFile = outputDir.toPath().resolve("content.txt");
        MyFileUtils.createNewFile(sourcePacksFile.toFile());

        StringBuilder fileContent = new StringBuilder("This pack was put together using GitHub Resourcepack Manager from the following packs:\n\n");

        for (File pack : sourcePacks) {
            fileContent.append("- ").append(StringUtils.nameWithoutPriorityString(pack)).append("\n");
        }

        try {
            Files.writeString(sourcePacksFile, fileContent);
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to write to file '%s'!", e, sourcePacksFile);
        }
    }

    private void extractSourcePacks(List<File> sourcePacks, File tempOutputDir) throws GithubResourcepackManagerException {
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

            LOGGER.error("'%s' is not a valid pack! Ignoring...", sourcePack);
            sourcePacks.remove(sourcePack);
        }
    }

    private List<File> gatherSourcePacksFrom(final Path packsDir) throws GithubResourcepackManagerException {
        // Gather resource packs
        final File[] sourcePacksArray = packsDir.toFile().listFiles();
        if (sourcePacksArray == null) throw new GithubResourcepackManagerException("Repository contains empty 'packs' folder!");

        // Return source packs sorted in correct order.
        return Stream.of(sourcePacksArray)
                .sorted(Comparator.comparingInt(StringUtils::extractPriorityFromFile).reversed())
                .toList();
    }

    /**
     * Gather list of {@link File}s to construct the final pack from.
     * <p>
     *     If there's a {@code pack.mcmeta} file in the {@link ModConfig#getResourcePackRoot() resource pack root directory}, the root directory will be the only file.
     * </p>
     * <p>
     *     If there's a {@link ModConfig#getResourcePackPacksDir() packs directory} in the pack root, all files directly in the packs directory will be returned.
     * </p>
     * <p>
     *     If there's neither a {@code pack.mcmeta} file or {@link ModConfig#getResourcePackPacksDir() packs directory} in the pack root, all files directly in the pack root will be returned.
     * </p>
     *
     * @return A list of {@link File}s to construct the final pack from. May include directories and .zip files.
     * @throws GithubResourcepackManagerException when the source packs could not be determined.
     */
    private List<File> gatherSourcePacks() throws GithubResourcepackManagerException {
        LOGGER.info("Checking for 'pack.mcmeta' in resource pack root...");
        final boolean hasPackMcmeta = config.getResourcePackRoot().resolve("pack.mcmeta").toFile().exists();
        LOGGER.info("%sFound!", hasPackMcmeta ? "" : "Not ");

        LOGGER.info("Checking for 'packs' directory in resource pack root...");
        Path packsDir = config.getResourcePackPacksDir();
        final boolean hasPacksFolder = Files.exists(packsDir) && Files.isDirectory(packsDir);
        LOGGER.info("%sFound!", hasPacksFolder ? "" : "Not ");

        if (hasPackMcmeta && hasPacksFolder) {
            throw new GithubResourcepackManagerException("Found both 'pack.mcmeta' and the 'packs' directory in resource pack root '%s'!", config.getResourcePackPacksDir().toAbsolutePath());
        }
        if (!hasPackMcmeta && !hasPacksFolder) {
            LOGGER.info("Found neither 'pack.mcmeta' nor the 'packs' directory in resource pack root '%s'!", config.getResourcePackPacksDir().toAbsolutePath());
            LOGGER.info("Assuming resource pack root '%s' as 'packs' directory.", config.getResourcePackPacksDir().toAbsolutePath());
            packsDir = config.getResourcePackRoot();
        }


        if (hasPackMcmeta) {
            LOGGER.info("Using resource pack root as resource pack.");
            return List.of(config.getResourcePackRoot().toFile());
        }

        LOGGER.info("Using 'packs' directory for resource packs.");
        return gatherSourcePacksFrom(packsDir);
    }


    private Path handleOldPackAndGetOutputPackPath(boolean wasUpdated, @Nullable Path oldPackPath, String oldPackName) {
        if (!wasUpdated) return oldPackPath;

        final String newPackName = generateRandomPackName(oldPackName);

        try {
            if (oldPackPath != null && Files.exists(oldPackPath)) Files.delete(oldPackPath);
        } catch (IOException e) {
            LOGGER.error("Failed to delete old pack!", new GithubResourcepackManagerException("Failed to delete old pack '%s'!", e, oldPackPath));
        }
        return RESOURCEPACK_OUTPUT_FOLDER.resolve(newPackName);
    }

    private String generateRandomPackName(@Nullable String oldPackNameString) {
        long oldPackName = -1;

        if (oldPackNameString != null) try {
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
