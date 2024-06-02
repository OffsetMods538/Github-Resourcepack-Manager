package top.offsetmonkey538.githubresourcepackmanager.utils;

import top.offsetmonkey538.githubresourcepackmanager.exception.GithubResourcepackManagerException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class WebhookSender {
    private WebhookSender() {

    }

    public static void send(String body, URI url, UpdateType updateType, boolean isUpdated) throws GithubResourcepackManagerException {
        final HttpRequest request = HttpRequest.newBuilder(url)
                .header("Content-Type", "application/json")
                .header("X-Resource-Pack-Update-Type", updateType.name())
                .header("X-Resource-Pack-Is-Updated", String.valueOf(isUpdated))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        final HttpClient client = HttpClient.newHttpClient();

        final HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new GithubResourcepackManagerException("Failed to send http request!", e);
        }

        final int statusCode = response.statusCode();
        if (!(statusCode >= 200 && statusCode < 300)) {
            throw new GithubResourcepackManagerException("Http status code '%s'! Response was: '%s'.", statusCode, response.body());
        }

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
