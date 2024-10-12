package top.offsetmonkey538.githubresourcepackmanager.networking.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import top.offsetmonkey538.githubresourcepackmanager.networking.TestNettyHandler;
import top.offsetmonkey538.githubresourcepackmanager.utils.HttpUtils;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

public class MainHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    public static final String NAME = TestNettyHandler.NAME + "/http-main";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (!request.decoderResult().isSuccess()) {
            HttpUtils.sendError(ctx, BAD_REQUEST);
            return;
        }


        final HttpMethod method = request.method();

        // GET request should go to fileserver
        if (method == HttpMethod.GET) {
            OtherNettyHandler.handleRequest(ctx, request);
            return;
        }

        // POST request should go to the webhook handler
        if (method == HttpMethod.POST) {
            WebhookHttpHandler.handleRequest(ctx, request);
            return;
        }

        // If we reach this point, then the request method isn't supported
        HttpUtils.sendError(ctx, METHOD_NOT_ALLOWED);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            HttpUtils.sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }
}
