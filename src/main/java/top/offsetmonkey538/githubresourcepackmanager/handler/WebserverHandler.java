package top.offsetmonkey538.githubresourcepackmanager.handler;

import io.undertow.Undertow;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import top.offsetmonkey538.githubresourcepackmanager.networking.MainHttpHandler;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.LOGGER;
import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.config;

public class WebserverHandler {

    public final Undertow webserver;

    public WebserverHandler() {
        this.webserver = Undertow.builder()
                .addHttpListener(config().webServerBindPort, config().webServerBindIp)
                .setHandler(new MainHttpHandler())
                .build();
    }

    public void initialize() {
        ServerLifecycleEvents.SERVER_STOPPING.register(minecraftServer -> {
            LOGGER.info("Stopping webserver!");
            webserver.stop();
        });

        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> {
            LOGGER.info("Starting webserver on {}:{}", config().webServerBindIp, config().webServerBindPort);
            webserver.start();
        });
    }
}
