package top.offsetmonkey538.gitpackmanager.common.config.webhook.discord.basic;

import top.offsetmonkey538.gitpackmanager.common.config.webhook.DefaultWebhookBody;

public final class BasicSuccessMessage implements DefaultWebhookBody {

    public final String username = "Git Pack Manager";
    public final String avatar_url = "https://github.com/OffsetMods538/Git-Pack-Manager/blob/master/common/src/main/resources/assets/git-pack-manager/icon.png?raw=true";
    public final String content = "New update for pack released!\nDescription: {shortDescription}\nDownload [here]({downloadUrl})";

    @Override
    public String getName() {
        return "discord/basic/success.json";
    }
}
