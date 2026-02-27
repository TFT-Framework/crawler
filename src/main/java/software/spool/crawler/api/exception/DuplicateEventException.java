package software.spool.crawler.api.exception;

public class DuplicateEventException extends SpoolException {
    private final String idempotencyKey;
    public DuplicateEventException(String key) {
        super("Duplicate event: " + key);
        this.idempotencyKey = key;
    }
    public String idempotencyKey() { return idempotencyKey; }
}