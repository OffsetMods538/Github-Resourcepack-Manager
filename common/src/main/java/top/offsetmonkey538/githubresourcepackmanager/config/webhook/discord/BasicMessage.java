package top.offsetmonkey538.githubresourcepackmanager.config.webhook.discord;

import top.offsetmonkey538.githubresourcepackmanager.config.webhook.DefaultWebhookBody;

public final class BasicMessage implements DefaultWebhookBody {

    public final String username = "GitHub Resource Pack Manager";
    public final String avatar_url = "https://github.com/OffsetMods538/Github-Resourcepack-Manager/blob/master/src/main/resources/assets/github-resourcepack-manager/icon.png?raw=true";
    public final String content = "New update for pack released!\nDescription: {shortDescription}\nDownload [here]({downloadUrl})";

    @Override
    public String getName() {
        return "discord/basic_message.json";
    }
}
