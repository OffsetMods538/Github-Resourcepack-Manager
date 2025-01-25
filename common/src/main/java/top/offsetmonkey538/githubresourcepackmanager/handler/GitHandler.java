package top.offsetmonkey538.githubresourcepackmanager.handler;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.githubresourcepackmanager.git.CommitProperties;

import java.io.IOException;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.*;
import static top.offsetmonkey538.githubresourcepackmanager.platform.PlatformLogging.LOGGER;

public class GitHandler {

    private CommitProperties commitProperties;
    private boolean wasUpdated;

    public void updateRepositoryAndGenerateCommitProperties() throws GithubResourcepackManagerException {
        String originalCommitHash;
        try {
            originalCommitHash = getLatestCommitHash();
        } catch (GithubResourcepackManagerException e) {
            if (!(e.getCause() instanceof RepositoryNotFoundException)) throw e;

            originalCommitHash = "";
        }

        updateRepository(true);

        final String newCommitHash = getLatestCommitHash();

        commitProperties = getLatestCommitProperties(originalCommitHash, newCommitHash);
        wasUpdated = !newCommitHash.equals(originalCommitHash);
    }


    private static CommitProperties getLatestCommitProperties(String lastCommitHash, String newCommitHash) throws GithubResourcepackManagerException {
        try {
            final Repository repository = getRepository();
            final RevCommit commit = new RevWalk(getRepository()).parseCommit(getLatestCommit().getObjectId());


            return new CommitProperties(
                    repository.getFullBranch(),
                    lastCommitHash,
                    newCommitHash,
                    commit.getAuthorIdent().getName(),
                    commit.getFullMessage().replace("\r\n", "\\n").replace("\n", "\\n"),
                    commit.getShortMessage(),
                    String.valueOf(commit.getCommitTime())
            );
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to parse latest commit!", e);
        }
    }

    private static void updateRepository(boolean retry) throws GithubResourcepackManagerException {
        // Create credentials provider if repository is private
        CredentialsProvider credentialsProvider = null;
        if (config.isRepoPrivate)
            credentialsProvider = new UsernamePasswordCredentialsProvider(config.githubUsername, config.githubToken);

        // If the repo folder doesn't exist, clone the repository.
        if (!REPO_ROOT_FOLDER.toFile().exists()) cloneRepository(credentialsProvider);

        // Pull from the remote
        boolean updateFailed = false;
        try (Git git = Git.open(REPO_ROOT_FOLDER.toFile())) {
            final PullResult result = git.pull()
                    .setCredentialsProvider(credentialsProvider)
                    .setContentMergeStrategy(ContentMergeStrategy.THEIRS)
                    .setStrategy(MergeStrategy.THEIRS)
                    .setRemoteBranchName(config.getGithubRef())
                    .call();

            // Handle errors
            if (result.isSuccessful()) {
                LOGGER.debug("Successfully updated repository!");
                return;
            }

            LOGGER.error("Failed to update repository!");
            updateFailed = true;
        } catch (GitAPIException e) {
            LOGGER.error("Failed to update repository!", e);
            updateFailed = true;
        } catch (IOException e) {
            LOGGER.error("Failed to open repository!", e);
            updateFailed = true;
        } finally {
            if (updateFailed && retry) {
                // Oh god this is so stupid
                //  Repository updating *should* only fail when remote repository is changed or
                //  some files are changed locally, so it should be fine to just delete and re-clone it.
                LOGGER.info("Deleting git folder and trying again...");

                try {
                    FileUtils.deleteDirectory(REPO_ROOT_FOLDER.toFile());
                } catch (IOException e) {
                    LOGGER.error("Failed to delete directory!", e);
                }

                updateRepository(false);
            }
        }
    }

    private static void cloneRepository(CredentialsProvider credentialsProvider) throws GithubResourcepackManagerException {
        try {
            Git git = Git.cloneRepository()
                    .setURI(config.repoUrl)
                    .setDirectory(REPO_ROOT_FOLDER.toFile())
                    .setBranch(config.getGithubRef())
                    .setCredentialsProvider(credentialsProvider)
                    .call();
            git.close();
        } catch (GitAPIException e) {
            throw new GithubResourcepackManagerException("Failed to clone repository!", e);
        }
    }

    private static String getLatestCommitHash() throws GithubResourcepackManagerException {
        return getLatestCommit().getObjectId().getName();
    }

    private static Ref getLatestCommit() throws GithubResourcepackManagerException {
        try {
            return getRepository().findRef("HEAD");
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to get latest commit in repository!", e);
        }
    }

    private static Repository getRepository() throws GithubResourcepackManagerException {
        try (Git git = Git.open(REPO_ROOT_FOLDER.toFile())) {
            return git.getRepository();
        } catch (IOException e) {
            throw new GithubResourcepackManagerException("Failed to open repository!", e);
        }
    }

    public CommitProperties getCommitProperties() {
        return commitProperties;
    }

    public boolean getWasUpdated() {
        return wasUpdated;
    }
}
