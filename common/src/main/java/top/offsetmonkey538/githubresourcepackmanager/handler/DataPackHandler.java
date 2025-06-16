package top.offsetmonkey538.githubresourcepackmanager.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.file.PathUtils;
import top.offsetmonkey538.githubresourcepackmanager.config.ModConfig;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformServerProperties;
import top.offsetmonkey538.githubresourcepackmanager.utils.MyFileUtils;
import top.offsetmonkey538.githubresourcepackmanager.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.*;
import static top.offsetmonkey538.githubresourcepackmanager.platform.PlatformLogging.LOGGER;

public class DataPackHandler {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Path STATE_FILE = DATAPACK_FOLDER.resolve("state.json");

    public void generatePack() throws GithubResourcepackManagerException {
        final Path datapacks = PlatformServerProperties.INSTANCE.getDatapacksDir();


        // Delete existing stuff
        final State existingPacks;
        try {
            existingPacks = readStateFile();
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to read state file!", e);
        }

        Arrays.stream(existingPacks.packs)
                .map(datapacks::resolve)
                .forEach(path -> {
                    if (Files.notExists(path)) {
                        LOGGER.warn("Not deleting pack at '%s' as it doesn't exist!", path.toAbsolutePath());
                        return;
                    }

                    try {
                        PathUtils.delete(path);
                        LOGGER.info("Deleted pack at '%s'!", path.toAbsolutePath());
                    } catch (IOException e) {
                        LOGGER.error("Failed to delete pack at '%s'!", e, path.toAbsolutePath());
                    }
                });


        // Get new packs
        final List<Path> sourcePacks;

        try {
            sourcePacks = gatherSourcePacks();
        } catch (GithubResourcepackManagerException e) {
            throw new GithubResourcepackManagerException("Failed to gather source packs!", e);
        }

        // Delete ones with same name from datapacks
        for (final Path sourcePack : sourcePacks) {
            final Path path = datapacks.resolve(sourcePack.getFileName());
            if (Files.notExists(path)) continue;

            LOGGER.info("Deleting pack at '%s' to replace it...", path.toAbsolutePath());
            try {
                PathUtils.delete(path);
                LOGGER.info("Deleted pack at '%s'!", path.toAbsolutePath());
            } catch (IOException e) {
                throw new GithubResourcepackManagerException("Failed to delete pack at '%s'!", e, path.toAbsolutePath());
            }
        }

        // Finally copy over the new ones
        for (final Path path : sourcePacks) {
            try {
                final Path destination = datapacks.resolve(path.getFileName());

                if (Files.isDirectory(path)) PathUtils.copyDirectory(path, destination);
                else Files.copy(path, destination);

                LOGGER.info("Copied pack from '%s' to '%s'.", path.toAbsolutePath(), destination.toAbsolutePath());
            } catch (IOException e) {
                throw new GithubResourcepackManagerException("Failed to copy pack at '%s' to datapacks directory at '%s'!", e, path.toAbsolutePath(), datapacks.toAbsolutePath());
            }
        }

        try {
            writeStateFile(sourcePacks);
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to write state file!", e);
        }
    }

    private void writeStateFile(List<Path> files) throws IOException {
        Files.createDirectories(STATE_FILE.getParent());

        final String json = GSON.toJson(new State(files.stream().map(Path::getFileName).map(Path::toString).toArray(String[]::new)));

        Files.writeString(STATE_FILE, json);
    }

    private State readStateFile() throws IOException {
        if (Files.notExists(STATE_FILE)) {
            LOGGER.warn("State file '%s' not found! No datapacks will be deleted!");
            return new State(new String[]{});
        }

        return GSON.fromJson(Files.readString(STATE_FILE), State.class);
    }

    private void copyPacksToDirectory(List<Path> sourcePacks, Path datapacksDir) throws GithubResourcepackManagerException {
        for (Path sourcePack : sourcePacks) {
            if (Files.isDirectory(sourcePack)) {
                try {
                    PathUtils.copyDirectory(sourcePack, datapacksDir);
                } catch (IOException e) {
                    throw new GithubResourcepackManagerException("Failed to copy pack '%s' into directory '%s'!", e, sourcePack, datapacksDir);
                }
                continue;
            }
            if (sourcePack.getFileName().endsWith(".zip")) {
                try {
                    Files.copy(sourcePack, datapacksDir);
                } catch (IOException e) {
                    throw new GithubResourcepackManagerException("Failed to copy pack '%s' into directory '%s'!", e, sourcePack, datapacksDir);
                }
                continue;
            }

            LOGGER.error("'%s' is not a valid pack! Ignoring...", sourcePack);
            sourcePacks.remove(sourcePack);
        }
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
    private List<Path> gatherSourcePacks() throws GithubResourcepackManagerException {
        LOGGER.info("Checking for 'pack.mcmeta' in data pack root...");
        final boolean hasPackMcmeta = Files.exists(config.dataPackProvider.getPackRoot().resolve("pack.mcmeta"));
        LOGGER.info("%sFound!", hasPackMcmeta ? "" : "Not ");

        LOGGER.info("Checking for 'packs' directory in data pack root...");
        Path packsDir = config.dataPackProvider.getPackPacksDir();
        final boolean hasPacksFolder = Files.exists(packsDir) && Files.isDirectory(packsDir);
        LOGGER.info("%sFound!", hasPacksFolder ? "" : "Not ");

        if (hasPackMcmeta && hasPacksFolder) {
            throw new GithubResourcepackManagerException("Found both 'pack.mcmeta' and the 'packs' directory in data pack root '%s'!", config.dataPackProvider.getPackRoot().toAbsolutePath());
        }
        if (!hasPackMcmeta && !hasPacksFolder) {
            LOGGER.info("Found neither 'pack.mcmeta' nor the 'packs' directory in data pack root '%s'!", config.dataPackProvider.getPackPacksDir().toAbsolutePath());
            LOGGER.info("Assuming data pack root '%s' as 'packs' directory.", config.dataPackProvider.getPackPacksDir().toAbsolutePath());
            packsDir = config.dataPackProvider.getPackRoot();
        }


        if (hasPackMcmeta) {
            LOGGER.info("Using data pack root as data pack.");
            return List.of(config.dataPackProvider.getPackRoot());
        }

        LOGGER.info("Using 'packs' directory for data packs.");
        return gatherSourcePacksFrom(packsDir);
    }

    private List<Path> gatherSourcePacksFrom(final Path packsDir) throws GithubResourcepackManagerException {
        try (final Stream<Path> sourcePacks = Files.list(packsDir)) {
            final List<Path> result = sourcePacks
                    .filter(path -> {
                        final boolean hidden = path.getFileName().startsWith(".");
                        if (!hidden) return true;
                        LOGGER.warn("Excluding hidden file '%s'", path.toAbsolutePath());
                        return false;
                    })
                    .toList();

            if (result.isEmpty())
                throw new GithubResourcepackManagerException("Repository contains empty 'packs' folder!");

            return result;
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to list files in 'packs' folder'!", e);
        }
    }


    private record State(String[] packs) {

    }
}
