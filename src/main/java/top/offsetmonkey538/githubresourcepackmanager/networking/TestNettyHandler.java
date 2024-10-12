package top.offsetmonkey538.githubresourcepackmanager.networking;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class TestNettyHandler extends ChannelInboundHandlerAdapter {
    public static final String NAME = "github-resourcepack-manager-fileserver";
    private final HttpServerCodec httpCodec = new HttpServerCodec();
    private final HttpObjectAggregator httpAggregator = new HttpObjectAggregator(65536);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object requestt) throws Exception {
        System.out.println(requestt);
        if (!(requestt instanceof ByteBuf buf)) {
            return;
        }
        buf = buf.copy();

        // Read the first line to check if it's an http request
        final StringBuilder firstLine = new StringBuilder();
        for (int i = 0; i < buf.readableBytes(); i++) {
            char currentChar = (char) buf.getByte(i);
            firstLine.append(currentChar);
            if (currentChar == '\n') break;
        }

        final boolean isHttp = firstLine.toString().contains("HTTP");
        System.out.println(firstLine);
        System.out.println("isHttp: " + isHttp);



        // If it's an http request, add the correct handlers
        if (isHttp) {
            ChannelPipeline pipeline = ctx.pipeline();

            // These need to be before Minecraft handlers,
            //  so add them to the front in reverse order.
            pipeline.addAfter(NAME, NAME + "/http-codec", new HttpServerCodec());
            pipeline.addAfter(NAME + "/http-codec", NAME + "/http-aggregator", new HttpObjectAggregator(65536));
            pipeline.addAfter(NAME + "/http-aggregator", NAME + "/actually-the-handler-now-lol", new OtherNettyHandler());
        }

        // This handler won't be needed anymore for this interaction
        ctx.pipeline().remove(NAME);

        // Forward to the next handler. If it isn't an http request and
        //  the http handlers weren't added above, it'll go to minecraft,
        //  else it'll go to the http stuffz
        ctx.fireChannelRead(requestt);
    }
}
