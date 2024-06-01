package top.offsetmonkey538.githubresourcepackmanager.networking;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.PathResourceManager;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.*;

public class MainHttpHandler implements HttpHandler {
    private final HttpHandler webhookHandler = new WebhookHttpHandler();
    private final HttpHandler fileHandler = Handlers.resource(new PathResourceManager(OUTPUT_FOLDER, 100));

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        LOGGER.debug("HTTP request: {}", exchange);

        // TODO: Add config option for an alias to the latest file somehow...
        if (config.webhookPath.equals(exchange.getRequestPath()))
            webhookHandler.handleRequest(exchange);
        else fileHandler.handleRequest(exchange);
    }
}
