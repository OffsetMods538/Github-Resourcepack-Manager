package top.offsetmonkey538.gitpackmanager.common.exception;

import static top.offsetmonkey538.offsetutils538.api.text.ArgReplacer.replaceArgs;

public class GitPackManagerException extends Exception {
    public GitPackManagerException(String message) {
        super(message);
    }

    public GitPackManagerException(String message, Object arg) {
        this(replaceArgs(message, arg));
    }

    public GitPackManagerException(String message, Object arg1, Object arg2) {
        this(replaceArgs(message, arg1, arg2));
    }

    public GitPackManagerException(String message, Object... args) {
        this(replaceArgs(message, args));
    }

    public GitPackManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public GitPackManagerException(String message, Throwable cause, Object arg) {
        this(replaceArgs(message, arg), cause);
    }

    public GitPackManagerException(String message, Throwable cause, Object arg1, Object arg2) {
        this(replaceArgs(message, arg1, arg2), cause);
    }

    public GitPackManagerException(String message, Throwable cause, Object... args) {
        this(replaceArgs(message, args), cause);
    }
}
