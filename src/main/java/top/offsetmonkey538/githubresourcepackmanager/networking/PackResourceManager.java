package top.offsetmonkey538.githubresourcepackmanager.networking;

import io.undertow.server.handlers.resource.*;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

import static top.offsetmonkey538.githubresourcepackmanager.GithubResourcepackManager.packHandler;

public class PackResourceManager extends PathResourceManager {
    public PackResourceManager(Path base) {
        super(base);
    }

    @Override
    public Resource getResource(String path) {
        return new PackResource(packHandler.getOutputPackPath(), this, path, path);
    }

    private static class PackResource extends PathResource {
        private final Path file;
        private final String fileName;

        public PackResource(Path file, PackResourceManager resourceManager, String path, @Nullable String fileName) {
            super(file, resourceManager, path);
            this.file = file;
            this.fileName = fileName;
        }

        @Override
        public String getName() {
            if (fileName != null) return fileName;
            return file.getFileName().toString();
        }
    }
}
