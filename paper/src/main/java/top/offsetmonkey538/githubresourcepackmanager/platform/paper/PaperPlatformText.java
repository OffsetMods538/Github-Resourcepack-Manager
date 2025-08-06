package top.offsetmonkey538.githubresourcepackmanager.platform.paper;

import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformText;
import top.offsetmonkey538.monkeylib538.api.text.MonkeyLibText;

public class PaperPlatformText implements PlatformText {
    @Override
    public void sendUpdateMessage(MonkeyLibText[] updateMessage, boolean adminsOnly) {
        for (final MonkeyLibText currentLine : updateMessage) {
            final PlayerList players = MinecraftServer.getServer().getPlayerList();
            if (!adminsOnly) {
                players.broadcastSystemMessage(Component.empty(), false);
                // TODO: once I implement paper version of monke: players.broadcastSystemMessage(PaperMonkeyLibText.of(currentLine).getText(), false);
                continue;
            }

            for (final ServerPlayer player : players.players) {
                if (!players.isOp(player.getGameProfile())) continue;
                player.sendSystemMessage(Component.empty());
                // TODO: once I implement paper version of monke: player.sendSystemMessage(PaperMonkeyLibText.of(currentLine).getText());
            }
        }
    }

    /*
    public void sendUpdateMessage(final String message, @Nullable final String hoverMessage, Map<String, String> placeholders, boolean adminsOnly) throws GithubResourcepackManagerException {
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


            final PlayerList players = MinecraftServer.getServer().getPlayerList();
            if (!adminsOnly) {
                players.broadcastSystemMessage(currentLine, false);
                continue;
            }

            for (final ServerPlayer player : players.players) {
                if (!players.isOp(player.getGameProfile())) continue;
                player.sendSystemMessage(currentLine);
            }
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
     */
}
