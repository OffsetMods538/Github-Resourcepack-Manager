package top.offsetmonkey538.githubresourcepackmanager.networking;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public final class WebhookHttpHandler {
    private WebhookHttpHandler() {}

    public static void handleRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        ctx.writeAndFlush(new DefaultFullHttpResponse(HTTP_1_1, OK)).addListener(ChannelFutureListener.CLOSE);
        GithubResourcepackManager.updatePack(GithubResourcepackManager.UpdateType.WEBHOOK, false);
    }
}
