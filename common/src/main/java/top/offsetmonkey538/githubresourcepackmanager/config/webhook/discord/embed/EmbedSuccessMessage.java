package top.offsetmonkey538.githubresourcepackmanager.config.webhook.discord.embed;

import top.offsetmonkey538.githubresourcepackmanager.config.webhook.DefaultWebhookBody;

public final class EmbedSuccessMessage implements DefaultWebhookBody {

    public final String username = "GitHub Resource Pack Manager";
    public final String avatar_url = "https://github.com/OffsetMods538/Github-Resourcepack-Manager/blob/master/src/main/resources/assets/github-resourcepack-manager/icon.png?raw=true";
    public final Embed[] embeds = new Embed[] {
            new Embed(
                    "New update for pack released!",
                    "Download [here]({downloadUrl})!",
                    0xFF2980,
                    new Field[] {
                            new Field(
                                    "Description",
                                    "{longDescription}"
                            )
                    }
            )
    };

    public record Embed(String title, String description, int color, Field[] fields) {}
    public record Field(String name, String value) {}

    @Override
    public String getName() {
        return "discord/embed/success.json";
    }
}
