package top.offsetmonkey538.githubresourcepackmanager.networking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;
import top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager;

import java.util.Arrays;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.LOGGER;
import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.config;

public class WebhookHttpHandler implements HttpHandler {
    private static final Gson GSON = new GsonBuilder().create();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        final HttpString requestMethod = exchange.getRequestMethod();

        // Ignore non-post requests
        if (!requestMethod.equalToString("POST")) return;
        exchange.setStatusCode(200);

        // Get the event header
        final HeaderValues githubEvent = exchange.getRequestHeaders().get("x-github-event");

        if (!githubEvent.contains("push")) return;
        LOGGER.debug("Received github push event");

        // Get payload
        exchange.getRequestReceiver().receiveFullString((exchange1, message) -> {
            final JsonObject payload = GSON.fromJson(message, JsonObject.class);

            // Check which branch was pushed to
            final String ref = payload.get("ref").getAsString();
            LOGGER.debug("Ref: " + ref);

            if (!config.githubRef.equals(ref)) return;

            LOGGER.debug("Tracked branch has been updated, updating local pack...");
            GithubResourcepackManager.updatePack(new GithubPushProperties(payload));
            LOGGER.debug("Local pack has been updated.");
        });
    }

    public record GithubPushProperties(
            String ref,
            String lastCommitHash,
            String newCommitHash,
            String repositoryName,
            String repositoryFullName,
            String repositoryUrl,
            String repositoryVisibility,
            String pusherName,
            String headCommitMessage
            ) {
        public GithubPushProperties(final JsonObject payload) {
            this(
                    getValue("ref", payload),
                    getValue("before", payload),
                    getValue("after", payload),
                    getValue("repository/name", payload),
                    getValue("repository/full_name", payload),
                    getValue("repository/html_url", payload),
                    getValue("repository/visibility", payload),
                    getValue("pusher/name", payload),
                    getValue("head_commit/message", payload)
            );
        }

        private static String getValue(String path, JsonObject jsonObject) {
            final String[] pathSeparated = path.split("/");

            final JsonElement currentElement = jsonObject.get(pathSeparated[0]);

            if (pathSeparated.length == 1) return currentElement.getAsString();
            if (!currentElement.isJsonObject()) {
                LOGGER.error("Expected json element '{}' to be an object!", currentElement);
                return "";
            }

            final String newPath = String.join("/", Arrays.copyOfRange(pathSeparated, 1, pathSeparated.length));
            return getValue(newPath, currentElement.getAsJsonObject());
        }
    }
}
