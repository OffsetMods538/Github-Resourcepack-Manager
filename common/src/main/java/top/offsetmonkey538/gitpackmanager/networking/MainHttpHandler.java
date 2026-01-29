package top.offsetmonkey538.gitpackmanager.networking;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import top.offsetmonkey538.gitpackmanager.GitPackManager;
import top.offsetmonkey538.meshlib.common.api.handler.HttpHandler;
import top.offsetmonkey538.meshlib.common.api.rule.HttpRule;
import top.offsetmonkey538.meshlib.common.api.util.HttpResponseUtil;

import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static top.offsetmonkey538.gitpackmanager.GitPackManager.resourcePackHandler;
import static top.offsetmonkey538.meshlib.common.api.util.HttpResponseUtil.sendError;
import static top.offsetmonkey538.meshlib.common.api.util.HttpResponseUtil.sendFile;

public class MainHttpHandler implements HttpHandler {
    @Override
    public void handleRequest(ChannelHandlerContext ctx, FullHttpRequest request, HttpRule rule) throws Exception {
        final HttpMethod method = request.method();

        // GET request should be sent the resource pack
        if (method == HttpMethod.GET) {
            if (resourcePackHandler.getOutputPackPath() == null) sendError(ctx, request, NOT_FOUND);
            else sendFile(ctx, request, resourcePackHandler.getOutputPackPath());
            return;
        }

        // POST request should trigger a pack update
        if (method == HttpMethod.POST) {
            HttpResponseUtil.sendResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, OK));
            GitPackManager.updatePack(GitPackManager.UpdateType.WEBHOOK, false);
            return;
        }

        // If we reach this point, then the request method isn't supported
        sendError(ctx, request, METHOD_NOT_ALLOWED);
    }
}
