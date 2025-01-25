package top.offsetmonkey538.githubresourcepackmanager.config.webhook;

public final class BasicWebhook implements DefaultWebhookBody {

    public final String ref = "{ref}";
    public final String lastCommitHash = "{lastCommitHash}";
    public final String newCommitHash = "{newCommitHash}";
    public final String author = "{author}";
    public final String longDescription = "{longDescription}";
    public final String shortDescription = "{shortDescription}";
    public final String timeOfCommit = "{timeOfCommit}";
    public final String downloadUrl = "{downloadUrl}";
    public final String updateType = "{updateType}";
    public final String wasUpdated = "{wasUpdated}";

    @Override
    public String getName() {
        return "basic_webhook.json";
    }
}
