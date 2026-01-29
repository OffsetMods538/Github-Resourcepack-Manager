package top.offsetmonkey538.gitpackmanager.exception;

public class GitPackManager extends Exception {
    public GitPackManager(String message) {
        super(message);
    }

    public GitPackManager(String message, Object... args) {
        this(String.format(message, args));
    }

    public GitPackManager(String message, Throwable cause) {
        super(message, cause);
    }

    public GitPackManager(String message, Throwable cause, Object... args) {
        this(String.format(message, args), cause);
    }
}
