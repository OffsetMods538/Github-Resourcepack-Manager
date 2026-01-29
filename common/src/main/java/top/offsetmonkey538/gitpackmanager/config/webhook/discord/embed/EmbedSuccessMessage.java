package top.offsetmonkey538.gitpackmanager.config.webhook.discord.embed;

import top.offsetmonkey538.gitpackmanager.config.webhook.DefaultWebhookBody;

public final class EmbedSuccessMessage implements DefaultWebhookBody {

    public final String username = "Git Pack Manager";
    public final String avatar_url = "https://github.com/OffsetMods538/Git-Pack-Manager/blob/master/common/src/main/resources/assets/git-pack-manager/icon.png?raw=true";
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
