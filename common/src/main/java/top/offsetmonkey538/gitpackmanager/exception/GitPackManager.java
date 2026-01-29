package top.offsetmonkey538.gitpackmanager.exception;

import static top.offsetmonkey538.offsetutils538.api.text.ArgReplacer.replaceArgs;

public class GitPackManager extends Exception {
    public GitPackManager(String message) {
        super(message);
    }

    public GitPackManager(String message, Object arg) {
        this(replaceArgs(message, arg));
    }

    public GitPackManager(String message, Object arg1, Object arg2) {
        this(replaceArgs(message, arg1, arg2));
    }

    public GitPackManager(String message, Object... args) {
        this(replaceArgs(message, args));
    }

    public GitPackManager(String message, Throwable cause) {
        super(message, cause);
    }

    public GitPackManager(String message, Throwable cause, Object arg) {
        this(replaceArgs(message, arg), cause);
    }

    public GitPackManager(String message, Throwable cause, Object arg1, Object arg2) {
        this(replaceArgs(message, arg1, arg2), cause);
    }

    public GitPackManager(String message, Throwable cause, Object... args) {
        this(replaceArgs(message, args), cause);
    }
}
