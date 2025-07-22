package top.offsetmonkey538.githubresourcepackmanager.networking;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;

import java.io.File;
import java.nio.file.Files;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.resourcePackHandler;
import static top.offsetmonkey538.githubresourcepackmanager.platform.PlatformLogging.LOGGER;

public final class FileHttpHandler {
    private FileHttpHandler() {}

    public static void handleRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        final File fileToServe = resourcePackHandler.getOutputPackFile();

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
                LOGGER.debug("%s Transfer Progress: %s/%s\n", future.channel(), progress, total);
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future) throws Exception {
                LOGGER.debug("%s Transfer complete!\n", future.channel());
            }
        });


        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
    }
}
