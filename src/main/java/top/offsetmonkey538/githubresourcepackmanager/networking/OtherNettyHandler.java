package top.offsetmonkey538.githubresourcepackmanager.networking;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.nio.file.Files;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class OtherNettyHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final File fileToServe = new File("/home/dave/testthingy/respak.zip");

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (!request.decoderResult().isSuccess()) {
            sendError(ctx, BAD_REQUEST);
            return;
        }

        if (request.method() != HttpMethod.GET) {
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }

        if (!fileToServe.exists() || !fileToServe.isFile()) {
            sendError(ctx, NOT_FOUND);
            return;
        }


        // Actually sendingt he file haha :D xd i have no idea what im doing and i don understandw aht chartgpu is tyelling me to do aaaacashahasahjfafjafnadasfsafdsafs

        final long fileLength = fileToServe.length();

        final HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        response.headers().set(CONTENT_LENGTH, fileLength);
        response.headers().set(CONTENT_TYPE, Files.probeContentType(fileToServe.toPath()));

        ctx.write(response);

        final ChannelFuture sendFileFuture = ctx.write(new DefaultFileRegion(fileToServe, 0, fileLength), ctx.newProgressivePromise());

        // progress
        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception {
                System.out.printf("%s Prongres: %s/%s\n", future.channel(), progress, total);
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future) throws Exception {
                System.out.printf("%s Transfer complete!\n", future.channel());
            }
        });


        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        final ByteBuf byteBuf = Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8);
        final FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, byteBuf);

        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
