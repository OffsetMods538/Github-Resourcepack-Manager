package top.offsetmonkey538.gitpackmanager.common.handler;

import org.apache.commons.io.file.PathUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.jspecify.annotations.Nullable;
import top.offsetmonkey538.gitpackmanager.common.exception.GitPackManagerException;
import top.offsetmonkey538.gitpackmanager.common.git.CommitProperties;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static top.offsetmonkey538.gitpackmanager.common.GitPackManager.GIT_FOLDER;
import static top.offsetmonkey538.gitpackmanager.common.GitPackManager.LOGGER;
import static top.offsetmonkey538.gitpackmanager.common.GitPackManager.config;
import static top.offsetmonkey538.offsetutils538.api.text.ArgReplacer.replaceArgs;

public class GitHandler {

    private @Nullable CommitProperties commitProperties = null;
    private @Nullable List<String> changedFiles;

    public void updateRepositoryAndGenerateCommitProperties() throws GitPackManagerException {
        String originalCommitHash;
        try {
            originalCommitHash = getLatestCommitHash();
        } catch (GitPackManagerException e) {
            if (!(e.getCause() instanceof RepositoryNotFoundException)) throw e;

            originalCommitHash = "";
        }

        updateRepository(true);

        final String newCommitHash = getLatestCommitHash();

        commitProperties = getLatestCommitProperties(originalCommitHash, newCommitHash);

        try {
            changedFiles = getDiff(originalCommitHash);
        } catch (Exception e) {
            // Most likely to happen with a fresh install or a forced update so should be fine to just assume this
            LOGGER.error("Failed to get diff between commit '%s' and '%s'! Assuming pack directories were updated anyway...", e, originalCommitHash, newCommitHash);
            changedFiles = null;
        }
    }


    private static CommitProperties getLatestCommitProperties(String lastCommitHash, String newCommitHash) throws GitPackManagerException {
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
            throw new GitPackManagerException("Failed to parse latest commit!", e);
        }
    }

    private static void updateRepository(boolean retry) throws GitPackManagerException {
        // Create credentials provider if repository is private
        CredentialsProvider credentialsProvider = null;
        if (config.get().repositoryInfo.isPrivate)
            credentialsProvider = new UsernamePasswordCredentialsProvider(config.get().repositoryInfo.username, config.get().repositoryInfo.token);

        // If the repo folder doesn't exist, clone the repository.
        if (!GIT_FOLDER.toFile().exists()) cloneRepository(credentialsProvider);

        // Pull from the remote
        boolean updateFailed = false;
        try (Git git = Git.open(GIT_FOLDER.toFile())) {
            final PullResult result = git.pull()
                    .setCredentialsProvider(credentialsProvider)
                    .setContentMergeStrategy(ContentMergeStrategy.THEIRS)
                    .setStrategy(MergeStrategy.THEIRS)
                    .setRemoteBranchName(config.get().getGithubRef())
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
                    PathUtils.deleteDirectory(GIT_FOLDER);
                } catch (IOException e) {
                    LOGGER.error("Failed to delete directory!", e);
                }

                updateRepository(false);
            }
        }
    }

    private static void cloneRepository(@Nullable CredentialsProvider credentialsProvider) throws GitPackManagerException {
        try {
            Git git = Git.cloneRepository()
                    .setURI(config.get().repositoryInfo.url)
                    .setDirectory(GIT_FOLDER.toFile())
                    .setBranch(config.get().getGithubRef())
                    .setCredentialsProvider(credentialsProvider)
                    .call();
            git.close();
        } catch (GitAPIException e) {
            throw new GitPackManagerException("Failed to clone repository!", e);
        }
    }

    private static String getLatestCommitHash() throws GitPackManagerException {
        return getLatestCommit().getObjectId().getName();
    }

    private static Ref getLatestCommit() throws GitPackManagerException {
        try {
            return getRepository().findRef("HEAD");
        } catch (IOException e) {
            throw new GitPackManagerException("Failed to get latest commit in repository!", e);
        }
    }

    private static List<String> getDiff(String startingHash) throws IOException, GitAPIException {
        try (Git git = Git.open(GIT_FOLDER.toFile())) {
            final Repository repository = git.getRepository();

            final ObjectId headCommit = repository.resolve("HEAD^{tree}");
            final ObjectId startingCommit = repository.resolve(startingHash + "^{tree}");
            if (startingCommit == null) throw new IllegalArgumentException(replaceArgs("Previous commit (hash '%s') doesn't exist!", startingHash));

            try (final ObjectReader repoReader = repository.newObjectReader()) {
                final CanonicalTreeParser headTreeParser = new CanonicalTreeParser();
                headTreeParser.reset(repoReader, headCommit);

                final CanonicalTreeParser startingTreeParser = new CanonicalTreeParser();
                startingTreeParser.reset(repoReader, startingCommit);

                return git
                        .diff()
                        .setNewTree(headTreeParser)
                        .setOldTree(startingTreeParser)
                        .call()

                        .stream()
                        .map(entry -> "/dev/null".equals(entry.getNewPath()) ? entry.getOldPath() : entry.getNewPath())
                        .toList();
            }
        } catch (IOException e) {
            throw new IOException("Failed to open repository!", e);
        }
    }

    private static Repository getRepository() throws GitPackManagerException {
        try (Git git = Git.open(GIT_FOLDER.toFile())) {
            return git.getRepository();
        } catch (IOException e) {
            throw new GitPackManagerException("Failed to open repository!", e);
        }
    }

    public @Nullable CommitProperties getCommitProperties() {
        return commitProperties;
    }

    public Optional<List<String>> getChangedFiles() {
        return Optional.ofNullable(changedFiles);
    }
}
