package top.offsetmonkey538.githubresourcepackmanager.platform.paper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.RGBLike;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformLogging;
import top.offsetmonkey538.githubresourcepackmanager.platform.PlatformMain;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.MOD_ID;
import static top.offsetmonkey538.githubresourcepackmanager.platform.PlatformLogging.LOGGER;

public class PaperPlatformMain implements PlatformMain {
    private static PaperPlugin plugin;

    @Override
    public Path getConfigDir() {
        return getPlugin().getDataPath();
    }

    @Override
    public void runOnServerStart(Runnable work) {
        work.run();
    }

    @Override
    public void registerLogToAdminListener() {
        LOGGER.addListener(PlatformLogging.LogLevel.ERROR, (message, error) -> {
            Component text = Component
                    .text(String.format("[%s] %s", MOD_ID, message))
                    .color(NamedTextColor.RED);

            if (error != null) text = text.hoverEvent(
                    HoverEvent.showText(
                            Component.text(ExceptionUtils.getRootCauseMessage(error))
                    )
            );


            boolean sent = false;
            for (final OfflinePlayer operator : plugin.getServer().getOperators()) {
                if (operator.getPlayer() == null) continue;
                operator.getPlayer().sendMessage(text);
                sent = true;
            }

            if (sent) return;

            plugin.messageQueue.addLast(text);
        });
    }

    public static void setPlugin(PaperPlugin plugin) {
        PaperPlatformMain.plugin = plugin;
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }


    //@Override
    //public void onInitializeServer() {
    //    GithubResourcepackManager.initialize();

    //    ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer1 -> minecraftServer = minecraftServer1);
    //}

    //public MinecraftServer getServer() {
    //    return minecraftServer;
    //}


    //@Override
    //public Logger getLogger() {
    //    return LOGGER;
    //}

    //@Override
    //public Path getConfigDir() {
    //    return FabricLoader.getInstance().getConfigDir();
    //}

    //@Override
    //public Path getGameDir() {
    //    return FabricLoader.getInstance().getGameDir();
    //}

    //@Override
    //public void runOnServerStart(Runnable work) {
    //    ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer1 -> work.run());
    //}
}
