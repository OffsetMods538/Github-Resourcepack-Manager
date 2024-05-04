package top.offsetmonkey538.githubresourcepackmanager;

import com.google.common.hash.Hashing;
import io.undertow.Undertow;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.offsetmonkey538.githubresourcepackmanager.config.ModConfig;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.githubresourcepackmanager.mixin.AbstractPropertiesHandlerAccessor;
import top.offsetmonkey538.githubresourcepackmanager.mixin.MinecraftDedicatedServerAccessor;
import top.offsetmonkey538.githubresourcepackmanager.mixin.ServerPropertiesLoaderAccessor;
import top.offsetmonkey538.githubresourcepackmanager.networking.MainHttpHandler;
import top.offsetmonkey538.githubresourcepackmanager.networking.WebhookHttpHandler;
import top.offsetmonkey538.githubresourcepackmanager.utils.GitManager;
import top.offsetmonkey538.githubresourcepackmanager.utils.MyFileUtils;
import top.offsetmonkey538.githubresourcepackmanager.utils.ZipUtils;
import top.offsetmonkey538.monkeylib538.config.ConfigManager;
import top.offsetmonkey538.monkeylib538.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;

public class GithubResourcepackManager implements DedicatedServerModInitializer {
	public static final String MOD_ID = "github-resourcepack-manager";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Path RESOURCEPACK_FOLDER = FabricLoader.getInstance().getGameDir().resolve("resourcepack");
	public static final Path REPO_ROOT_FOLDER = RESOURCEPACK_FOLDER.resolve("git");
	public static final Path PACKS_FOLDER = REPO_ROOT_FOLDER.resolve("packs");
	public static final Path OUTPUT_FOLDER = RESOURCEPACK_FOLDER.resolve("output");
	public static final Pattern PACK_NAME_PATTERN = Pattern.compile("\\d+-");

	private static MinecraftDedicatedServer minecraftServer;
	private static boolean minecraftServerStarted;

	public static ModConfig config;

	@Override
	public void onInitializeServer() {
		config = ConfigManager.init(new ModConfig(), LOGGER::error);

		if (config.serverPublicIp == null || config.githubUrl == null || (config.isPrivate && (config.githubUsername == null || config.githubToken == null))) {
			LOGGER.error("Please fill in the config file!");
			throw new RuntimeException("Please fill in the config file!");
		}

		try {
			if (!OUTPUT_FOLDER.toFile().exists()) Files.createDirectories(OUTPUT_FOLDER);
		} catch (IOException e) {
			LOGGER.error("Couldn't create output directory!");
			throw new RuntimeException(e);
		}


		final Undertow webServer = Undertow.builder()
				.addHttpListener(config.webServerBindPort, config.webServerBindIp)
				.setHandler(new MainHttpHandler())
				.build();

		setupEvents(webServer);
	}

	private static void setupEvents(final Undertow webServer) {
		ServerLifecycleEvents.SERVER_STOPPING.register(minecraftServer -> {
			LOGGER.info("Stopping webserver!");
			webServer.stop();
		});

		ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> {
			GithubResourcepackManager.minecraftServer = (MinecraftDedicatedServer) minecraftServer;

			updatePack(null);

			LOGGER.info("Starting webserver on {}:{}", config.webServerBindIp, config.webServerBindPort);
			webServer.start();
		});

		ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> GithubResourcepackManager.minecraftServerStarted = true);
	}

	public static void updatePack(@Nullable WebhookHttpHandler.GithubPushProperties pushProperties) {
		LOGGER.info("Updating resourcepack...");

		final String outputFileName = Math.abs(new Random().nextLong()) + ".zip";
		final File outputFile = new File(OUTPUT_FOLDER.toFile(), outputFileName);
		LOGGER.debug("New pack name: {}", outputFileName);

		if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
			LOGGER.error("Failed to create output folder!");
			return;
		}

		cleanOutputDirectory();
        try {
            GitManager.updateRepository(true);
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to update git repository!", e);
			return;
        }
        try {
			createThePack(outputFile);
		} catch (GithubResourcepackManagerException e) {
			LOGGER.error("Failed to create final pack!", e);
			return;
        }

        try {
            afterPackUpdate(outputFileName, outputFile, pushProperties);
        } catch (GithubResourcepackManagerException e) {
            LOGGER.error("Failed to complete tasks after updating pack!", e);
			return;
        }

        LOGGER.info("Resourcepack updated!");
	}

	private static void afterPackUpdate(final String outputFileName, final File outputFile, @Nullable WebhookHttpHandler.GithubPushProperties pushProperties) throws GithubResourcepackManagerException {
		if (minecraftServer == null) return;

		// We're probably on a webserver thread, so
		//  we want to run on the minecraft server thread
		AtomicReference<GithubResourcepackManagerException> failure = new AtomicReference<>(null);
		minecraftServer.execute(() -> {
            try {
                updateResourcePackProperties(outputFileName, outputFile);
            } catch (GithubResourcepackManagerException e) {
				failure.set(new GithubResourcepackManagerException("Failed to update resource pack properties!", e));
				return;
            }

            if (!minecraftServerStarted) return;

			String message = config.packUpdateMessage;
			if (pushProperties != null) {
				message = message.replace("{ref}", pushProperties.ref());
				message = message.replace("{lastCommitHash}", pushProperties.lastCommitHash());
				message = message.replace("{newCommitHash}", pushProperties.newCommitHash());
				message = message.replace("{repositoryName}", pushProperties.repositoryName());
				message = message.replace("{repositoryFullName}", pushProperties.repositoryFullName());
				message = message.replace("{repositoryUrl}", pushProperties.repositoryUrl());
				message = message.replace("{repositoryVisibility}", pushProperties.repositoryVisibility());
				message = message.replace("{pusherName}", pushProperties.pusherName());
				message = message.replace("{headCommitMessage}", pushProperties.headCommitMessage());
			}

			final String[] splitMessage = message.split("\n");

			for (int lineNumber = 0; lineNumber < splitMessage.length; lineNumber++) {
				final String currentLineString = splitMessage[lineNumber];
				final Text currentLine;
                try {
                    currentLine = TextUtils.INSTANCE.getStyledText(currentLineString);
                } catch (Exception e) {
                    failure.set(new GithubResourcepackManagerException("Failed to style update message at line number '%s'!", e, lineNumber));
					return;
                }

                minecraftServer.getPlayerManager().broadcast(currentLine, false);
			}
		});
		if (failure.get() != null) throw failure.get();
	}

	private static void updateResourcePackProperties(final String outputFileName, final File outputFile) throws GithubResourcepackManagerException {
		final ServerPropertiesLoader propertiesLoader = ((MinecraftDedicatedServerAccessor) minecraftServer).getPropertiesLoader();

		final String resourcePackUrl = String.format(
				"http://%s:%s/%s",
				config.serverPublicIp,
				config.webServerBindPort,
				outputFileName
		);
		final String resourcePackSha1;
		try {
			//noinspection deprecation
			resourcePackSha1 = Hashing.sha1().hashBytes(com.google.common.io.Files.toByteArray(outputFile)).toString();
		} catch (IOException e) {
			throw new GithubResourcepackManagerException("Failed to get sha1 hash from pack file '%s'!", e, outputFile);
		}

		LOGGER.info("Saving new resource pack properties to 'server.properties' file...");
		LOGGER.info("New resource pack url: '{}'", resourcePackUrl);
		LOGGER.info("New resource pack sha1: '{}'", resourcePackSha1);
		propertiesLoader.apply(properties -> {
			final Properties serverProperties = ((AbstractPropertiesHandlerAccessor) properties).getProperties();

			serverProperties.setProperty("resource-pack", resourcePackUrl);
			serverProperties.setProperty("resource-pack-sha1", resourcePackSha1);

			return properties;
		});
		LOGGER.info("New resource pack properties saved!");

		LOGGER.info("Reloading properties from 'server.properties' file...");
		final ServerPropertiesLoaderAccessor propertiesLoaderAccess = (ServerPropertiesLoaderAccessor) propertiesLoader;
		propertiesLoaderAccess.setPropertiesHandler(ServerPropertiesHandler.load(propertiesLoaderAccess.getPath()));
		LOGGER.info("Properties from 'server.properties' file reloaded!");
	}

	private static void cleanOutputDirectory() {
		final File[] files = OUTPUT_FOLDER.toFile().listFiles();
		if (files == null) return;
		for (File file : files) {
			//noinspection ResultOfMethodCallIgnored
			file.delete();
		}
	}

	private static void createThePack(File outputFile) throws GithubResourcepackManagerException {
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
			createPackFromRepositoryRoot(outputFile);
		} else {
			LOGGER.info("Using 'packs' directory for resource packs.");
			createPackFromPacksFolder(outputFile);
		}
	}

	private static void createPackFromRepositoryRoot(File outputFile) throws GithubResourcepackManagerException {
		zipItUp(REPO_ROOT_FOLDER.toFile(), outputFile);
	}

	private static void createPackFromPacksFolder(File outputFile) throws GithubResourcepackManagerException {
		// Gather resource packs in correct order
		final File[] sourcePacksArray = PACKS_FOLDER.toFile().listFiles();
		if (sourcePacksArray == null) {
			LOGGER.error("Repository contains empty 'packs' folder!");
			return;
		}

		final List<File> sourcePacks = Stream.of(sourcePacksArray)
				.sorted(Comparator.comparingInt(GithubResourcepackManager::extractPriorityFromFile).reversed())
				.toList();

		// Create tmp directory
        final Path tmpDir;
        try {
            tmpDir = Files.createTempDirectory("github-resourcepack-manager");
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to create temporary directory!", e);
        }
        final File packsExtractDir = MyFileUtils.createDir(tmpDir.resolve("extractedPacks").toFile());
		final File outputDir = MyFileUtils.createDir(tmpDir.resolve("output").toFile());

		// Extract all source packs
		for (File pack : sourcePacks) {
			if (pack.isDirectory()) {
                try {
                    FileUtils.copyDirectory(pack, outputDir);
                } catch (IOException e) {
                    throw new GithubResourcepackManagerException("Failed to copy pack '%s' to '%s'!", e, pack, outputDir);
                }
            }
			else if (pack.getName().endsWith(".zip")) extractPack(pack, MyFileUtils.createDir(new File(packsExtractDir, pack.getName())), outputDir);
			else LOGGER.error("'{}' is not a valid pack! Ignoring...", pack);
		}

		// Generate file with source pack names
		final Path inputPacksFilePath = outputDir.toPath().resolve("content.txt");
		MyFileUtils.createNewFile(inputPacksFilePath.toFile());

		StringBuilder fileContent = new StringBuilder("This pack was put together from the following packs:\n\n");

		for (File pack : sourcePacks) {
			fileContent.append("- ").append(nameWithoutPriorityString(pack)).append("\n");
		}

        try {
            Files.writeString(inputPacksFilePath, fileContent);
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to write to file '%s'!", e, inputPacksFilePath);
        }

        zipItUp(outputDir, outputFile);
	}

	private static void extractPack(File pack, File tempExtractDir, File finalOutputDir) throws GithubResourcepackManagerException {
		ZipUtils.unzipFile(pack, tempExtractDir);

		try {
			FileUtils.copyDirectory(tempExtractDir, finalOutputDir);
		} catch (IOException e) {
			LOGGER.error("Couldn't copy pack " + pack, e);

			throw new GithubResourcepackManagerException("Failed to copy pack '%s' to '%s'!", e, pack, finalOutputDir);
		}
	}

	private static int extractPriorityFromFile(File file) {
		final String filename = file.getName();

		final Matcher matcher = PACK_NAME_PATTERN.matcher(filename);

		if (!matcher.find()) {
			LOGGER.error("File '" + file + "' doesn't start with priority!");
			return -1;
		}

		return Integer.parseInt(matcher.group().replace('-', ' ').strip());
	}

	public static String nameWithoutPriorityString(File file) {
		final String filename = file.getName();

		final Matcher matcher = PACK_NAME_PATTERN.matcher(filename);

		if (!matcher.find()) throw new RuntimeException("File '" + file + "' doesn't start with priority!");

		return filename.replace(matcher.group(), "").strip();
	}

	private static void zipItUp(File source, File outputFile) throws GithubResourcepackManagerException {
		try {
			if (!source.exists()) Files.createDirectories(source.toPath());

			final FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
			final ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

			ZipUtils.zipDirectory(source, zipOutputStream);

			zipOutputStream.close();
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			throw new GithubResourcepackManagerException("Failed to find file '%s'!", e, outputFile);
		} catch (IOException e) {
			throw new GithubResourcepackManagerException("Failed to zip resource pack!", e);
		}
	}
}
