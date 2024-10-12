package top.offsetmonkey538.githubresourcepackmanager.mixin;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.offsetmonkey538.githubresourcepackmanager.networking.TestNettyHandler;

@Mixin(targets = "net/minecraft/server/ServerNetworkIo$1")
public class ServerNetworkIoMixin {

    @Inject(
            method = "initChannel",
            at = @At("TAIL")
    )
    private void test(Channel channel, CallbackInfo ci) {
        ChannelPipeline pipeline = channel.pipeline();
        //pipeline.addLast("http-codec", new HttpServerCodec());
        //pipeline.addLast("http-aggregator", new HttpObjectAggregator(65536));
        pipeline.addFirst(TestNettyHandler.NAME, new TestNettyHandler());
    }
}
