package top.offsetmonkey538.gitpackmanager.config.webhook.discord.embed;

import top.offsetmonkey538.gitpackmanager.config.webhook.DefaultWebhookBody;

public final class EmbedFailMessage implements DefaultWebhookBody {

    public final String username = "Git Pack Manager";
    public final String avatar_url = "https://github.com/OffsetMods538/Git-Pack-Manager/blob/master/common/src/main/resources/assets/git-pack-manager/icon.png?raw=true";
    public final Embed[] embeds = new Embed[] {
            new Embed(
                    "Pack update failed!",
                    0xFF0000
            )
    };

    public record Embed(String title, int color) {}

    @Override
    public String getName() {
        return "discord/embed/fail.json";
    }
}
