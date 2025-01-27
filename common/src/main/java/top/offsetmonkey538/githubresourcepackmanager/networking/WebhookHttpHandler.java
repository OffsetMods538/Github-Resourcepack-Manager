package top.offsetmonkey538.githubresourcepackmanager.networking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager;
import top.offsetmonkey538.meshlib.api.HttpHandler;

import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.config;
import static top.offsetmonkey538.githubresourcepackmanager.platform.PlatformLogging.LOGGER;

public final class WebhookHttpHandler {
    private WebhookHttpHandler() {}

    private static final Gson GSON = new GsonBuilder().create();

    public static void handleRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (!"application/json".contentEquals(HttpUtil.getMimeType(request))) {
            LOGGER.warn(String.format("Bad request: POST request made with incorrect mime type '%s', expected 'application/json'", HttpUtil.getMimeType(request)));
            HttpHandler.sendError(ctx, BAD_REQUEST);
            return;
        }


        // Get the event header
        final String githubEvent = request.headers().get("x-github-event");

        if (githubEvent == null || githubEvent.isBlank()) {
            HttpHandler.sendError(ctx, BAD_REQUEST, "Request headers don't contain 'x-github-event'");
            return;
        }

        if (!githubEvent.contains("push")) {
            HttpHandler.sendError(ctx, BAD_REQUEST, "Request isn't for push event");
            return;
        }
        LOGGER.debug("Received github push event");

        // Get payload
        final JsonObject payload = GSON.fromJson(request.content().toString(StandardCharsets.UTF_8), JsonObject.class);
        if (payload == null) {
            HttpHandler.sendError(ctx, BAD_REQUEST, "Request doesn't contain content");
            return;
        }

        // Respond with "yeh bro everythins alright"
        ctx.writeAndFlush(new DefaultFullHttpResponse(HTTP_1_1, OK)).addListener(ChannelFutureListener.CLOSE);

        // Check which branch was pushed to
        final String ref = payload.get("ref").getAsString();
        LOGGER.debug("Ref: %s", ref);

        if (!config.getGithubRef().equals(ref)) return;

        LOGGER.debug("Tracked branch has been updated, updating local pack...");
        GithubResourcepackManager.updatePack(GithubResourcepackManager.UpdateType.WEBHOOK);
        LOGGER.debug("Local pack has been updated.");
    }
}
