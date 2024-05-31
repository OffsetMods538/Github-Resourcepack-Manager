package top.offsetmonkey538.githubresourcepackmanager.utils;

import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.LOGGER;

public final class WebhookSender {
    private WebhookSender() {

    }

    public static void send(String body, URI url, UpdateType updateType, boolean isUpdated) throws GithubResourcepackManagerException {
        System.out.println(body);

        final HttpRequest request = HttpRequest.newBuilder(url)
                .header("Content-Type", "application/json")
                .header("X-Resource-Pack-Update-Type", updateType.name())
                .header("X-Resource-Pack-Is-Updated", String.valueOf(isUpdated))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        final HttpClient client = HttpClient.newHttpClient();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::statusCode)
                .thenAccept(response -> LOGGER.info("Http status code: {}", response)); // TODO: check if status code is a failure and whatever

        // From JDK 21 the HttpClient class extends AutoCloseable, but as we want to support Minecraft versions
        //  that use JDK 17, where HttpClient doesn't extend AutoCloseable, we need to check if it's
        //  an instance of AutoCloseable before trying to close it.
        //noinspection ConstantValue
        if (client instanceof AutoCloseable) {
            try {
                ((AutoCloseable) client).close();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public enum UpdateType {
        RESTART,
        RUNTIME
    }
}
