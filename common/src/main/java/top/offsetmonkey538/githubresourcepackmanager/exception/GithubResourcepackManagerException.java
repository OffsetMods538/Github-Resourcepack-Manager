package top.offsetmonkey538.githubresourcepackmanager.exception;

public class GithubResourcepackManagerException extends Exception {
    public GithubResourcepackManagerException(String message) {
        super(message);
    }

    public GithubResourcepackManagerException(String message, Object... args) {
        this(String.format(message, args));
    }

    public GithubResourcepackManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public GithubResourcepackManagerException(String message, Throwable cause, Object... args) {
        this(String.format(message, args), cause);
    }
}
