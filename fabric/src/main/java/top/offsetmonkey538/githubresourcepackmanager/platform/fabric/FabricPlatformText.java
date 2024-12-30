package top.offsetmonkey538.githubresourcepackmanager.platform.fabric;

import net.minecraft.server.PlayerManager;
import net.minecraft.text.*;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformText;
import top.offsetmonkey538.githubresourcepackmanager.utils.StringUtils;
import top.offsetmonkey538.monkeylib538.utils.TextUtils;

import java.util.Map;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.config;

public class FabricPlatformText implements PlatformText {
    @Override
    public void sendUpdateMessage(Map<String, String> placeholders) throws GithubResourcepackManagerException {
        final PlayerManager playerManager = FabricPlatformMain.INSTANCE.getServer().getPlayerManager();
        if (playerManager == null) return;

        String message = config.packUpdateMessage;
        final String[] splitMessage = message.split("\n");

        final HoverEvent hoverEvent;
        try {
            hoverEvent = config.packUpdateMessageHoverMessage == null ? null : new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    TextUtils.INSTANCE.getStyledText(
                            StringUtils.replacePlaceholders(config.packUpdateMessageHoverMessage, placeholders).replace("\\n", "\n")
                    )
            );
        } catch (Exception e) {
            throw new GithubResourcepackManagerException("Failed to style update hover message!", e);
        }

        for (int lineNumber = 0; lineNumber < splitMessage.length; lineNumber++) {
            final String currentLineString = StringUtils.replacePlaceholders(splitMessage[lineNumber], placeholders).replace("\\n", "\n");
            final MutableText currentLine = Text.empty();
            try {
                for (Text currentLineSibling : TextUtils.INSTANCE.getStyledText(currentLineString).getSiblings()) {
                    final MutableText sibling = currentLineSibling.copy();

                    if (hoverEvent != null) sibling.setStyle(sibling.getStyle().withHoverEvent(hoverEvent));

                    final String siblingString = sibling.getString();
                    if (!siblingString.contains("{packUpdateCommand}")) {
                        currentLine.append(sibling);
                        continue;
                    }

                    final Style siblingStyle = sibling.getStyle();
                    final String[] splitSibling = siblingString.split("\\{packUpdateCommand}");

                    if (splitSibling.length > 0)
                        currentLine.append(Text.literal(splitSibling[0]).setStyle(siblingStyle));

                    currentLine.append(Text.literal("[HERE]").setStyle(siblingStyle
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Click to update pack")))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gh-rp-manager request-pack"))
                    ));

                    if (splitSibling.length > 1)
                        currentLine.append(Text.literal(splitSibling[1]).setStyle(siblingStyle));
                }
            } catch (Exception e) {
                throw new GithubResourcepackManagerException("Failed to style update message at line number '%s'!", e, lineNumber);
            }

            playerManager.broadcast(currentLine, false);
        }
    }
}
