package top.offsetmonkey538.githubresourcepackmanager.networking.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import top.offsetmonkey538.githubresourcepackmanager.networking.TestNettyHandler;

import java.io.File;
import java.nio.file.Files;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public final class OtherNettyHandler {
    private OtherNettyHandler() {}

    public static final String NAME = TestNettyHandler.NAME + "/http-fileserver";

    private static final File fileToServe = new File("/home/dave/testthingy/respak.zip");

    public static void handleRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
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
}
