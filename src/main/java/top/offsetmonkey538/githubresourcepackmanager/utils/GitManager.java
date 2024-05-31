package top.offsetmonkey538.githubresourcepackmanager.utils;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;

import java.io.IOException;
import java.util.Map;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.REPO_ROOT_FOLDER;
import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.LOGGER;
import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.config;

public final class GitManager {
    private GitManager() {

    }

    public static CommitProperties getLatestCommitProperties(String lastCommitHash, String newCommitHash) throws GithubResourcepackManagerException {
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

    public static String getLatestCommitHash() throws GithubResourcepackManagerException {
        return getLatestCommit().getObjectId().getName();
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
                    .setURI(config.githubUrl)
                    .setDirectory(REPO_ROOT_FOLDER.toFile())
                    .setBranch(config.githubRef)
                    .setCredentialsProvider(credentialsProvider)
                    .call();
            git.close();
        } catch (GitAPIException e) {
            throw new GithubResourcepackManagerException("Failed to clone repository!", e);
        }
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

    public record CommitProperties(
            String ref,
            String lastCommitHash,
            String newCommitHash,
            String author,
            String longDescription,
            String shortDescription,
            String timeOfCommit
    ) {
        public Map<String, String> toPlaceholdersMap() {
            return Map.ofEntries(
                    Map.entry("{ref}",               ref),
                    Map.entry("{lastCommitHash}",    lastCommitHash),
                    Map.entry("{newCommitHash}",     newCommitHash),
                    Map.entry("{author}",            author),
                    Map.entry("{longDescription}",   longDescription),
                    Map.entry("{shortDescription}",  shortDescription),
                    Map.entry("{timeOfCommit}",      timeOfCommit),
                    Map.entry("{pusherName}",        author),
                    Map.entry("{headCommitMessage}", longDescription)
            );
        }
    }
}
