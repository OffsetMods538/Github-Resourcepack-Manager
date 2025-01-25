package top.offsetmonkey538.githubresourcepackmanager.platform.paper;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformText;
import top.offsetmonkey538.githubresourcepackmanager.utils.StringUtils;

import java.util.List;
import java.util.Map;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.config;

public class PaperPlatformText implements PlatformText {
    @Override
    public void sendUpdateMessage(Map<String, String> placeholders) throws GithubResourcepackManagerException {
        String message = config.packUpdateMessage;
        final String[] splitMessage = message.split("\n");

        final HoverEvent hoverEvent;
        try {
            hoverEvent = config.packUpdateMessageHoverMessage == null ? null : new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    getStyledText(
                            StringUtils.replacePlaceholders(config.packUpdateMessageHoverMessage, placeholders).replace("\\n", "\n")
                    )
            );
        } catch (Exception e) {
            throw new GithubResourcepackManagerException("Failed to style update hover message!", e);
        }

        for (int lineNumber = 0; lineNumber < splitMessage.length; lineNumber++) {
            final String currentLineString = StringUtils.replacePlaceholders(splitMessage[lineNumber], placeholders).replace("\\n", "\n");
            final MutableComponent currentLine = Component.empty();
            try {
                for (Component currentLineSibling : getStyledText(currentLineString).getSiblings()) {
                    final MutableComponent sibling = currentLineSibling.copy();

                    if (hoverEvent != null) sibling.setStyle(sibling.getStyle().withHoverEvent(hoverEvent));

                    final String siblingString = sibling.getString();
                    if (!siblingString.contains("{packUpdateCommand}")) {
                        currentLine.append(sibling);
                        continue;
                    }

                    final Style siblingStyle = sibling.getStyle();
                    final String[] splitSibling = siblingString.split("\\{packUpdateCommand}");

                    if (splitSibling.length > 0)
                        currentLine.append(Component.literal(splitSibling[0]).setStyle(siblingStyle));

                    currentLine.append(Component.literal("[HERE]").setStyle(siblingStyle
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to update pack")))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gh-rp-manager request-pack"))
                    ));

                    if (splitSibling.length > 1)
                        currentLine.append(Component.literal(splitSibling[1]).setStyle(siblingStyle));
                }
            } catch (Exception e) {
                throw new GithubResourcepackManagerException("Failed to style update message at line number '%s'!", e, lineNumber);
            }

            MinecraftServer.getServer().getPlayerList().broadcastSystemMessage(currentLine, false);
        }
    }


    private static final Style DEFAULT_STYLE = Style.EMPTY.withItalic(false).withColor(ChatFormatting.WHITE);

    private static MutableComponent getStyledText(String text) throws Exception {
        final MutableComponent result = Component.empty();
        Style style = DEFAULT_STYLE;

        boolean isFormattingCode = false;
        boolean isEscaped = false;
        char[] characters = text.toCharArray();
        for (int characterIndex = 0; characterIndex < characters.length; characterIndex++) {
            char currentChar = characters[characterIndex];

            if (isFormattingCode) {
                // Hex color
                if (currentChar == '#') {
                    if (characterIndex + 7 >= characters.length)
                        throw new Exception("Unfinished hex code starting at character number '" + characterIndex + "'!");

                    try {
                        style = style.withColor(TextColor.parseColor(text.substring(characterIndex, characterIndex + 7)).getOrThrow(Exception::new));
                    } catch (Exception e) {
                        throw new Exception("Failed to parse hex color starting at character number '" + characterIndex + "'!", e);
                    }

                    // Move pointer 6 characters ahead as we already read the whole hex code
                    characterIndex += 6;
                    isFormattingCode = false;
                    continue;
                }

                style = getStyleForFormattingCode(currentChar, style);

                if (style == null)
                    throw new Exception("Invalid formatting code at character number '" + characterIndex + "'!");

                isFormattingCode = false;
                continue;
            }

            if (!isEscaped) {
                switch (currentChar) {
                    case '&':
                        isFormattingCode = true;
                        continue;
                    case '\\':
                        isEscaped = true;
                        continue;
                }
            }
            isEscaped = false;


            final List<Component> siblings = result.getSiblings();
            final int lastSiblingIndex = siblings.size() - 1;
            final Component lastSibling = siblings.isEmpty() ? Component.empty() : siblings.get(lastSiblingIndex);

            // Check if the style of the last sibling is the same as the current one
            if (!siblings.isEmpty() && lastSibling.getStyle().equals(style)) {
                // If so, set the last sibling to itself plus the new character
                siblings.set(lastSiblingIndex, Component.literal(lastSibling.getString() + currentChar).setStyle(style));
            } else {
                // Otherwise, just append a new sibling to the result
                result.append(Component.literal(String.valueOf(currentChar)).setStyle(style));
            }
        }

        return result;
    }

    private static Style getStyleForFormattingCode(char formattingCode, Style currentStyle) {
        if (formattingCode == 'r') return DEFAULT_STYLE;

        final ChatFormatting formatting = ChatFormatting.getByCode(formattingCode);
        if (formatting == null) return null;

        return currentStyle.applyFormat(formatting);
    }
}
