package top.offsetmonkey538.githubresourcepackmanager.platform.fabric;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformText;
import top.offsetmonkey538.githubresourcepackmanager.utils.StringUtils;

import java.util.List;
import java.util.Map;

public class FabricPlatformText implements PlatformText {
    Style DEFAULT_STYLE = Style.EMPTY.withItalic(false).withColor(Formatting.WHITE);


    @Override
    public void sendUpdateMessage(final String message, @Nullable final String hoverMessage, Map<String, String> placeholders, boolean adminsOnly) throws GithubResourcepackManagerException {
        final PlayerManager playerManager = FabricPlatformMain.getServer().getPlayerManager();
        if (playerManager == null) return;

        final String[] splitMessage = message.split("\n");

        final HoverEvent hoverEvent;
        try {
            hoverEvent = hoverMessage == null ? null : new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    getStyledText(
                            StringUtils.replacePlaceholders(hoverMessage, placeholders).replace("\\n", "\n")
                    )
            );
        } catch (Exception e) {
            throw new GithubResourcepackManagerException("Failed to style update hover message!", e);
        }

        for (int lineNumber = 0; lineNumber < splitMessage.length; lineNumber++) {
            final String currentLineString = StringUtils.replacePlaceholders(splitMessage[lineNumber], placeholders).replace("\\n", "\n");
            final MutableText currentLine = Text.empty();
            try {
                for (Text currentLineSibling : getStyledText(currentLineString).getSiblings()) {
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


            if (!adminsOnly) {
                playerManager.broadcast(currentLine, false);
                continue;
            }

            for (final ServerPlayerEntity player : playerManager.getPlayerList()) {
                if (!playerManager.isOperator(player.getGameProfile())) continue;
                player.sendMessageToClient(currentLine, false);
            }
        }
    }

    private MutableText getStyledText(String text) throws Exception {
        final MutableText result = Text.empty();
        Style style = DEFAULT_STYLE;

        boolean isFormattingCode = false;
        boolean isEscaped = false;
        char[] characters = text.toCharArray();
        for (int characterIndex = 0; characterIndex < characters.length; characterIndex++) {
            char currentChar = characters[characterIndex];

            if (isFormattingCode) {
                // Hex color
                if (currentChar == '#') {
                    if (characterIndex + 7 >= characters.length) throw new Exception("Unfinished hex code starting at character number '" + characterIndex + "'!");

                    try {
                        style = style.withColor(parseHexColor(text.substring(characterIndex, characterIndex + 7)));
                    } catch (Exception e) {
                        throw new Exception("Failed to parse hex color starting at character number '" + characterIndex + "'!", e);
                    }

                    // Move pointer 6 characters ahead as we already read the whole hex code
                    characterIndex += 6;
                    isFormattingCode = false;
                    continue;
                }

                style = getStyleForFormattingCode(currentChar, style);

                if (style == null) throw new Exception("Invalid formatting code at character number '" + characterIndex + "'!");

                isFormattingCode = false;
                continue;
            }

            if (!isEscaped){
                switch (currentChar){
                    case '&':
                        isFormattingCode = true;
                        continue;
                    case '\\':
                        isEscaped = true;
                        continue;
                }
            }
            isEscaped = false;


            final List<Text> siblings = result.getSiblings();
            final int lastSiblingIndex = siblings.size() - 1;
            final Text lastSibling = siblings.isEmpty() ? Text.empty() : siblings.get(lastSiblingIndex);

            // Check if the style of the last sibling is the same as the current one
            if (!siblings.isEmpty() && lastSibling.getStyle().equals(style)) {
                // If so, set the last sibling to itself plus the new character
                siblings.set(lastSiblingIndex, Text.literal(lastSibling.getString() + currentChar).setStyle(style));
            } else {
                // Otherwise, just append a new sibling to the result
                result.append(Text.literal(String.valueOf(currentChar)).setStyle(style));
            }
        }

        return result;
    }


    private Style getStyleForFormattingCode(char formattingCode, Style currentStyle) {
        if (formattingCode == 'r') return DEFAULT_STYLE;

        final Formatting formatting = Formatting.byCode(formattingCode);
        if (formatting == null) return null;

        return currentStyle.withFormatting(formatting);
    }

    public TextColor parseHexColor(String hexColor) throws Exception {
        return TextColor.parse(hexColor).getOrThrow(Exception::new);
    }
}
