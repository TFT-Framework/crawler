package software.spool.crawler.api.exception;

public abstract class SpoolException extends RuntimeException {
    protected SpoolException(String message) {
        super(message);
    }
    protected SpoolException(String message, Throwable cause) {super(message);}
}
