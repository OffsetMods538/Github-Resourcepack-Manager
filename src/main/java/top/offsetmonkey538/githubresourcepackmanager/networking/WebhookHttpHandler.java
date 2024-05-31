package top.offsetmonkey538.githubresourcepackmanager.networking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;
import top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager;

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
            GithubResourcepackManager.updatePack(true);
            LOGGER.debug("Local pack has been updated.");
        });
    }
}
