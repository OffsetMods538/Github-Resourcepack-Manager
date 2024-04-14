package top.offsetmonkey538.githubresourcepackmanager;

import com.google.common.hash.Hashing;
import io.undertow.Undertow;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.offsetmonkey538.githubresourcepackmanager.config.ModConfig;
import top.offsetmonkey538.githubresourcepackmanager.mixin.AbstractPropertiesHandlerMixin;
import top.offsetmonkey538.githubresourcepackmanager.mixin.ServerPropertiesHandlerMixin;
import top.offsetmonkey538.githubresourcepackmanager.networking.MainHttpHandler;
import top.offsetmonkey538.githubresourcepackmanager.utils.GitManager;
import top.offsetmonkey538.githubresourcepackmanager.utils.MyFileUtils;
import top.offsetmonkey538.githubresourcepackmanager.utils.ZipUtils;
import top.offsetmonkey538.monkeylib538.config.ConfigManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;

public class GithubResourcepackManager implements DedicatedServerModInitializer {
	public static final String MOD_ID = "github-resourcepack-manager";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Path RESOURCEPACK_FOLDER = FabricLoader.getInstance().getGameDir().resolve("resourcepack");
	public static final Path GIT_FOLDER = RESOURCEPACK_FOLDER.resolve("git");
	public static final Path PACKS_FOLDER = GIT_FOLDER.resolve("packs");
	public static final Path OUTPUT_FOLDER = RESOURCEPACK_FOLDER.resolve("output");
	public static final Pattern PACK_NAME_PATTERN = Pattern.compile("\\d+-");

	private static MinecraftDedicatedServer minecraftServer;
	private static boolean minecraftServerStarted;

	public static ModConfig config;

	@Override
	public void onInitializeServer() {
		config = ConfigManager.init(new ModConfig(), LOGGER::error);

		if (config.githubUrl == null || (config.isPrivate && (config.githubUsername == null || config.githubToken == null))) {
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
				.addHttpListener(config.serverPort, config.serverIp)
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

			updatePack();

			LOGGER.info("Starting webserver on {}:{}", config.serverIp, config.serverPort);
			webServer.start();
		});

		ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> GithubResourcepackManager.minecraftServerStarted = true);
	}

	public static void updatePack() {
		LOGGER.info("Updating resourcepack...");

		final String outputFileName = Math.abs(new Random().nextLong()) + ".zip";
		final File outputFile = new File(OUTPUT_FOLDER.toFile(), outputFileName);

		if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
			LOGGER.error("Couldn't create output folder!");
		}

		cleanOutputDirectory();
		GitManager.updateRepository(true);
		try {
			createThePack(outputFile);
		} catch (IOException e) {
            throw new RuntimeException("Couldn't create final pack!", e);
        }

        afterPackUpdate(outputFileName, outputFile);

		LOGGER.info("Resourcepack updated!");
	}

	private static void afterPackUpdate(final String outputFileName, final File outputFile) {
		if (minecraftServer == null) return;

		// We're probably on a webgithubserver thread, so
		//  we want to run on the minecraft server thread
		minecraftServer.execute(() -> {
			updateResourcePackProperties(outputFileName, outputFile);

			if (!minecraftServerStarted) return;

			minecraftServer.getPlayerManager().broadcast(Text.of("Server resourcepack has been updated!"), false);
			minecraftServer.getPlayerManager().broadcast(Text.of("Please rejoin the server to get the most up to date pack."), false);
		});
	}

	private static void updateResourcePackProperties(final String outputFileName, final File outputFile) {
		try {
			final Optional<MinecraftServer.ServerResourcePackProperties> originalOptional = minecraftServer.getProperties().serverResourcePackProperties;

			if (originalOptional.isEmpty()) return;
			final MinecraftServer.ServerResourcePackProperties original = originalOptional.get();

			//noinspection deprecation
			((ServerPropertiesHandlerMixin) minecraftServer.getProperties()).setServerResourcePackProperties(
					Optional.of(
							new MinecraftServer.ServerResourcePackProperties(
									((AbstractPropertiesHandlerMixin) minecraftServer.getProperties()).invokeGetString("resource-pack", "").replace("pack.zip", outputFileName),
									Hashing.sha1().hashBytes(com.google.common.io.Files.toByteArray(outputFile)).toString(),
									original.isRequired(),
									original.prompt()
							)
					)
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void cleanOutputDirectory() {
		final File[] files = OUTPUT_FOLDER.toFile().listFiles();
		if (files == null) return;
		for (File file : files) {
			//noinspection ResultOfMethodCallIgnored
			file.delete();
		}
	}

	private static void createThePack(File outputFile) throws IOException {
		if (!PACKS_FOLDER.toFile().exists()) {
			LOGGER.error("Repository doesn't contain 'packs' folder!");
			return;
		}
		if (!PACKS_FOLDER.toFile().isDirectory()) {
			LOGGER.error("Repository contains 'packs' file, expected a folder!");
			return;
		}

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
		final Path tmpDir = Files.createTempDirectory("github-resourcepack-manager");
		final File packsExtractDir = MyFileUtils.createDir(tmpDir.resolve("extractedPacks").toFile());
		final File outputDir = MyFileUtils.createDir(tmpDir.resolve("output").toFile());

		// Extract all source packs
		for (File pack : sourcePacks) {
			if (pack.isDirectory()) FileUtils.copyDirectory(pack, outputDir);
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

		Files.writeString(inputPacksFilePath, fileContent);

		zipItUp(outputDir, outputFile);
	}

	private static void extractPack(File pack, File tempExtractDir, File finalOutputDir) {
        try {
            ZipUtils.unzipFile(pack, tempExtractDir);
        } catch (IOException e) {
            LOGGER.error("Couldn't unzip pack " + pack, e);
        }

        try {
            FileUtils.copyDirectory(tempExtractDir, finalOutputDir);
        } catch (IOException e) {
			LOGGER.error("Couldn't copy pack " + pack, e);
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

	private static void zipItUp(File source, File outputFile) {
		try {
			if (!source.exists()) Files.createDirectories(source.toPath());

			final FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
			final ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

			ZipUtils.zipDirectory(source, zipOutputStream);

			zipOutputStream.close();
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			LOGGER.error("Failed to find file!", e);
		} catch (IOException e) {
			LOGGER.error("Failed to zip resourcepack!", e);
		}
	}
}
