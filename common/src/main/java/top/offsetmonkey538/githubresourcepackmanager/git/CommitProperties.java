package top.offsetmonkey538.githubresourcepackmanager.git;

import java.util.Map;

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
                Map.entry("{ref}", ref),
                Map.entry("{lastCommitHash}", lastCommitHash),
                Map.entry("{newCommitHash}", newCommitHash),
                Map.entry("{author}", author),
                Map.entry("{longDescription}", longDescription),
                Map.entry("{shortDescription}", shortDescription),
                Map.entry("{timeOfCommit}", timeOfCommit),
                Map.entry("{pusherName}", author),
                Map.entry("{headCommitMessage}", longDescription)
        );
    }
}
