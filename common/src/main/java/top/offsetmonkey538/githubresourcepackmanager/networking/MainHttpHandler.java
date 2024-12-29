package top.offsetmonkey538.githubresourcepackmanager.networking;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import org.jetbrains.annotations.NotNull;
import top.offsetmonkey538.meshlib.api.HttpHandler;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

public class MainHttpHandler implements HttpHandler {

    @Override
    public void handleRequest(@NotNull ChannelHandlerContext ctx, @NotNull FullHttpRequest request) throws Exception {
        if (!request.decoderResult().isSuccess()) {
            HttpHandler.sendError(ctx, BAD_REQUEST);
            return;
        }


        final HttpMethod method = request.method();

        // GET request should go to fileserver
        if (method == HttpMethod.GET) {
            FileHttpHandler.handleRequest(ctx, request);
            return;
        }

        // POST request should go to the webhook handler
        if (method == HttpMethod.POST) {
            WebhookHttpHandler.handleRequest(ctx, request);
            return;
        }

        // If we reach this point, then the request method isn't supported
        HttpHandler.sendError(ctx, METHOD_NOT_ALLOWED);
    }
}
