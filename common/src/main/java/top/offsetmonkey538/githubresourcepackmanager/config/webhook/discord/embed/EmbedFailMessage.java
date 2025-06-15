package top.offsetmonkey538.githubresourcepackmanager.config.webhook.discord.embed;

import top.offsetmonkey538.githubresourcepackmanager.config.webhook.DefaultWebhookBody;

public final class EmbedFailMessage implements DefaultWebhookBody {

    public final String username = "GitHub Resource Pack Manager";
    public final String avatar_url = "https://github.com/OffsetMods538/Github-Resourcepack-Manager/blob/master/src/main/resources/assets/github-resourcepack-manager/icon.png?raw=true";
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
