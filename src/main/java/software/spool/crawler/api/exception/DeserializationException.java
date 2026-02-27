package software.spool.crawler.api.exception;

public class DeserializationException extends SpoolException {
    private final String rawPayload;
    public DeserializationException(String rawPayload, Throwable cause) {
        super("Deserialization failed for: " + rawPayload, cause);
        this.rawPayload = rawPayload;
    }
    public DeserializationException(String message, String rawPayload) {
        super(message);
        this.rawPayload = rawPayload;
    }
    public String rawPayload() { return rawPayload; }
}