package top.offsetmonkey538.gitpackmanager.networking;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import org.jspecify.annotations.NonNull;
import top.offsetmonkey538.meshlib.common.api.handler.HttpHandler;
import top.offsetmonkey538.meshlib.common.api.rule.HttpRule;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static top.offsetmonkey538.meshlib.common.api.util.HttpResponseUtil.sendError;

public class MainHttpHandler implements HttpHandler {

    @Override
    public void handleRequest(@NonNull ChannelHandlerContext ctx, @NonNull FullHttpRequest request, @NonNull HttpRule rule) throws Exception {
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
        sendError(ctx, request, METHOD_NOT_ALLOWED);
    }
}
