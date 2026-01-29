package top.offsetmonkey538.gitpackmanager.handler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.jspecify.annotations.Nullable;
import top.offsetmonkey538.gitpackmanager.exception.GitPackManagerException;
import top.offsetmonkey538.gitpackmanager.utils.MyFileUtils;
import top.offsetmonkey538.gitpackmanager.utils.StringUtils;
import top.offsetmonkey538.gitpackmanager.utils.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Stream;

import static top.offsetmonkey538.gitpackmanager.GitPackManager.LOGGER;
import static top.offsetmonkey538.gitpackmanager.GitPackManager.RESOURCEPACK_FOLDER;
import static top.offsetmonkey538.gitpackmanager.GitPackManager.RESOURCEPACK_OUTPUT_FOLDER;
import static top.offsetmonkey538.gitpackmanager.GitPackManager.config;

public class ResourcePackHandler {
    private @Nullable Path outputPackPath = null;

    public void generatePack(boolean wasUpdated, @Nullable Path oldPackPath, @Nullable String oldPackName) throws GitPackManagerException {
        outputPackPath = handleOldPackAndGetOutputPackPath(wasUpdated, oldPackPath, oldPackName);

        // If using the old pack, don't generate a new one.
        if (!wasUpdated) return;

        // Generate new one
        generateNewPack();
    }

    private void generateNewPack() throws GitPackManagerException {
        final List<File> sourcePacks;

        try {
            sourcePacks = gatherSourcePacks();
        } catch (GitPackManagerException e) {
            throw new GitPackManagerException("Failed to gather source packs!", e);
        }


        // Create temp directories to extract pack(s) into.
        final Path tmpDir;
        try {
            tmpDir = Files.createDirectories(RESOURCEPACK_FOLDER.resolve(".temp"));
        } catch (IOException e) {
            throw new GitPackManagerException("Failed to create temporary directory!", e);
        }
        final File tempOutputDir = MyFileUtils.createDir(tmpDir.resolve("output").toFile());

        // Extract packs into the temporary packs directory
        try {
            extractSourcePacks(sourcePacks, tempOutputDir);
        } catch (GitPackManagerException e) {
            throw new GitPackManagerException("Failed to extract source packs!", e);
        }

        // Write file with source pack names
        try {
            writeSourcePacksFile(sourcePacks, tempOutputDir);
        } catch (GitPackManagerException e) {
            throw new GitPackManagerException("Failed to write pack content file!", e);
        }

        // Zip the pack content and put the output file in the output directory.
        try {
            ZipUtils.zipDirectory(tempOutputDir, Objects.requireNonNull(getOutputPackFile()));
        } catch (GitPackManagerException e) {
            throw new GitPackManagerException("Failed to zip pack content!", e);
        }

        // Delete temp directories.
        try {
            FileUtils.deleteDirectory(tmpDir.toFile());
        } catch (IOException e) {
            throw new GitPackManagerException("Failed to delete temporary directory!", e);
        }
    }

    private void writeSourcePacksFile(List<File> sourcePacks, File outputDir) throws GitPackManagerException {
        if (sourcePacks.size() == 1) return;

        final Path sourcePacksFile = outputDir.toPath().resolve("content.txt");
        MyFileUtils.createNewFile(sourcePacksFile.toFile());

        StringBuilder fileContent = new StringBuilder("This pack was put together using Git Pack Manager from the following packs:\n\n");

        for (File pack : sourcePacks) {
            fileContent.append("- ").append(StringUtils.nameWithoutPriorityString(pack)).append("\n");
        }

        try {
            Files.writeString(sourcePacksFile, fileContent);
        } catch (IOException e) {
            throw new GitPackManagerException("Failed to write to file '%s'!", e, sourcePacksFile);
        }
    }

    private void extractSourcePacks(List<File> sourcePacks, File tempOutputDir) throws GitPackManagerException {
        // Extract source packs
        for (File sourcePack : sourcePacks) {
            if (sourcePack.isDirectory()) {
                try {
                    FileUtils.copyDirectory(sourcePack, tempOutputDir, HiddenFileFilter.VISIBLE);
                } catch (IOException e) {
                    throw new GitPackManagerException("Failed to copy pack '%s' into output directory '%s'!", e, sourcePack, tempOutputDir);
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

    private List<File> gatherSourcePacksFrom(final Path packsDir) throws GitPackManagerException {
        // Gather resource packs
        final File[] sourcePacksArray = packsDir.toFile().listFiles();
        if (sourcePacksArray == null) throw new GitPackManagerException("Repository contains empty 'packs' folder!");

        // Return source packs sorted in correct order.
        return Stream.of(sourcePacksArray)
                .filter(file -> {
                    final boolean hidden = file.getName().startsWith(".");
                    if (!hidden) return true;
                    LOGGER.warn("Excluding hidden file '%s'", file.getAbsolutePath());
                    return false;
                })
                .sorted(Comparator.comparingInt(StringUtils::extractPriorityFromFile).reversed())
                .toList();
    }

    private List<File> gatherSourcePacks() throws GitPackManagerException {
        LOGGER.info("Checking for 'pack.mcmeta' in resource pack root...");
        final boolean hasPackMcmeta = config.get().resourcePackProvider.getPackRoot().resolve("pack.mcmeta").toFile().exists();
        LOGGER.info("%sFound!", hasPackMcmeta ? "" : "Not ");

        LOGGER.info("Checking for 'packs' directory in resource pack root...");
        Path packsDir = config.get().resourcePackProvider.getPackPacksDir();
        final boolean hasPacksFolder = Files.exists(packsDir) && Files.isDirectory(packsDir);
        LOGGER.info("%sFound!", hasPacksFolder ? "" : "Not ");

        if (hasPackMcmeta && hasPacksFolder) {
            throw new GitPackManagerException("Found both 'pack.mcmeta' and the 'packs' directory in resource pack root '%s'!", config.get().resourcePackProvider.getPackPacksDir().toAbsolutePath());
        }
        if (!hasPackMcmeta && !hasPacksFolder) {
            LOGGER.info("Found neither 'pack.mcmeta' nor the 'packs' directory in resource pack root '%s'!", config.get().resourcePackProvider.getPackPacksDir().toAbsolutePath());
            LOGGER.info("Assuming resource pack root '%s' as 'packs' directory.", config.get().resourcePackProvider.getPackPacksDir().toAbsolutePath());
            packsDir = config.get().resourcePackProvider.getPackRoot();
        }


        if (hasPackMcmeta) {
            LOGGER.info("Using resource pack root as resource pack.");
            return List.of(config.get().resourcePackProvider.getPackRoot().toFile());
        }

        LOGGER.info("Using 'packs' directory for resource packs.");
        return gatherSourcePacksFrom(packsDir);
    }


    private @Nullable Path handleOldPackAndGetOutputPackPath(boolean wasUpdated, @Nullable Path oldPackPath, @Nullable String oldPackName) {
        if (!wasUpdated) return oldPackPath;

        final String newPackName = generateRandomPackName(oldPackName);

        try {
            if (oldPackPath != null && Files.exists(oldPackPath)) Files.delete(oldPackPath);
        } catch (IOException e) {
            LOGGER.error("Failed to delete old pack!", new GitPackManagerException("Failed to delete old pack '%s'!", e, oldPackPath));
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

    public @Nullable Path getOutputPackPath() {
        return outputPackPath;
    }

    public @Nullable File getOutputPackFile() {
        return getOutputPackPath() == null ? null : getOutputPackPath().toFile();
    }

    public @Nullable String getOutputPackName() {
        return getOutputPackFile() == null ? null : getOutputPackFile().getName();
    }
}
