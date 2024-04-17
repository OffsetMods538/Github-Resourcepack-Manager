package top.offsetmonkey538.githubresourcepackmanager.utils;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;

import java.io.IOException;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.REPO_ROOT_FOLDER;
import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.LOGGER;
import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.config;

public final class GitManager {
    private GitManager() {

    }

    public static void updateRepository(boolean retry) throws GithubResourcepackManagerException {
        CredentialsProvider credentialsProvider = null;
        if (config.isPrivate)
            credentialsProvider = new UsernamePasswordCredentialsProvider(config.githubUsername, config.githubToken);

        if (!REPO_ROOT_FOLDER.toFile().exists()) cloneRepository(credentialsProvider);

        try (Git git = Git.open(REPO_ROOT_FOLDER.toFile())) {
            final PullResult result = git.pull()
                    .setCredentialsProvider(credentialsProvider)
                    .setContentMergeStrategy(ContentMergeStrategy.THEIRS)
                    .setStrategy(MergeStrategy.THEIRS)
                    .setRemoteBranchName(config.githubRef)
                    .call();
            if (result.isSuccessful()) {
                LOGGER.debug("Successfully updated repository!");
                return;
            }
            LOGGER.error("Failed to update repository!");
        } catch (GitAPIException e) {
            LOGGER.error("Failed to update repository!", e);

            // FIXME: Oh god this is so stupid
            //  I guess this should only happen when the local clone is modified so it's fine -_o_-
            if (!retry) {
                throw new GithubResourcepackManagerException("Failed to update repository!", e);
            }
            LOGGER.info("Deleting git folder and trying again...");

            try {
                FileUtils.deleteDirectory(REPO_ROOT_FOLDER.toFile());
            } catch (IOException ex) {
                LOGGER.error("Failed to delete directory!", e);
            }

            updateRepository(false);
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to open repository!", e);
        }
    }

    private static void cloneRepository(CredentialsProvider credentialsProvider) throws GithubResourcepackManagerException {
        try {
            Git git = Git.cloneRepository()
                    .setURI(config.githubUrl.endsWith(".git") ? config.githubUrl : config.githubUrl + ".git")
                    .setDirectory(REPO_ROOT_FOLDER.toFile())
                    .setBranch(config.githubRef)
                    .setCredentialsProvider(credentialsProvider)
                    .call();
            git.close();
        } catch (GitAPIException e) {
            throw new GithubResourcepackManagerException("Failed to clone repository!", e);
        }
    }
}
