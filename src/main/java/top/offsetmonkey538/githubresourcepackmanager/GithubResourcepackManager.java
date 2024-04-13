package top.offsetmonkey538.githubresourcepackmanager;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.offsetmonkey538.githubresourcepackmanager.config.ModConfig;
import top.offsetmonkey538.monkeylib538.config.ConfigManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GithubResourcepackManager implements DedicatedServerModInitializer {
	public static final String MOD_ID = "github-resourcepack-manager";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Path RESOURCEPACK_FOLDER = FabricLoader.getInstance().getGameDir().resolve("resourcepack");
	public static final Path GIT_FOLDER = RESOURCEPACK_FOLDER.resolve("git");
	public static final Path OUTPUT_FOLDER = RESOURCEPACK_FOLDER.resolve("output");


	public static ModConfig config;

	@Override
	public void onInitializeServer() {
		config = ConfigManager.init(new ModConfig(), LOGGER::error);

		if (config.githubUrl == null || config.githubUsername == null || config.githubToken == null) {
			LOGGER.error("Please fill in the config file!");
			throw new RuntimeException("Please fill in the config file!");
		}

		try {
			if (!OUTPUT_FOLDER.toFile().exists()) Files.createDirectories(OUTPUT_FOLDER);
		} catch (IOException e) {
			LOGGER.error("Couldn't create output directory!");
			throw new RuntimeException(e);
		}

		updatePack();
	}

	public static void updatePack() {
		updateRepository();
		zipThePack();
	}

	public static void updateRepository() {
		final CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(config.githubUsername, config.githubToken);

		if (GIT_FOLDER.toFile().exists()) {
			try	(Git git = Git.open(GIT_FOLDER.toFile())) {
				final PullResult result = git.pull()
						.setCredentialsProvider(credentialsProvider)
						.call();
				if (result.isSuccessful()) {
					LOGGER.info("Successfully updated repository!");
					return;
				}
				LOGGER.info("Failed to update repository!");
			} catch (GitAPIException e) {
				LOGGER.error("Failed to update repository!", e);
			} catch (IOException e) {
				LOGGER.error("Failed to open repository!", e);
			}

			return;
		}

		try {
			Git git = Git.cloneRepository()
					.setURI(config.githubUrl.endsWith(".git") ? config.githubUrl : config.githubUrl + ".git")
					.setDirectory(GIT_FOLDER.toFile())
					.setCredentialsProvider(credentialsProvider)
					.call();
			git.close();
		} catch (GitAPIException e) {
			LOGGER.error("Failed to clone repository!", e);
		}
	}

	public static void zipThePack() {
		try {
			final File pack = new File(OUTPUT_FOLDER.toFile(), "pack.zip");

			final FileOutputStream fileOutputStream = new FileOutputStream(pack);
			final ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

			zipFile(GIT_FOLDER.toFile(), "", zipOutputStream);

			zipOutputStream.close();
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			LOGGER.error("Failed to find file!", e);
		} catch (IOException e) {
			LOGGER.error("Failed to zip resourcepack!", e);
		}
	}

	private static void zipFile(File fileToZip, String filename, ZipOutputStream zipOutputStream) throws IOException {
		if (fileToZip.isHidden()) return;

		if (fileToZip.isDirectory()) {
			filename = filename.endsWith("/") ? filename : filename + "/";

			zipOutputStream.putNextEntry(new ZipEntry(filename));
			zipOutputStream.closeEntry();

			final File[] children = fileToZip.listFiles();
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

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
