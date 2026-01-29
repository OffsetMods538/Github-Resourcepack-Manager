package top.offsetmonkey538.gitpackmanager.utils;

import top.offsetmonkey538.gitpackmanager.GitPackManager;
import top.offsetmonkey538.gitpackmanager.exception.GitPackManagerException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class WebhookSender {
    private WebhookSender() {}

    public static void send(String body, URI url, GitPackManager.UpdateType updateType, boolean updateSucceeded) throws GitPackManagerException {
        final HttpRequest request = HttpRequest.newBuilder(url)
                .header("Content-Type", "application/json")
                .header("X-Resource-Pack-Update-Type", updateType.name())
                .header("X-Resource-Pack-Update-Succeeded", String.valueOf(updateSucceeded))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        final HttpResponse<String> response;
        // I think we now depend on JDK 21 so HttpClient will always be AutoCloseable
        try (final HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new GitPackManagerException("Failed to send http request!", e);
        }

        final int statusCode = response.statusCode();
        if (statusCode < 200 || statusCode >= 300) {
            throw new GitPackManagerException("Http status code '%s'! Response was: '%s'.", statusCode, response.body());
        }
    }
}
