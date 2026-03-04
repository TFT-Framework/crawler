package software.spool.crawler.internal.utils;

import software.spool.core.exception.SpoolException;
import software.spool.crawler.api.port.InboxWriter;

import java.util.HashMap;
import java.util.Map;

/**
 * In-memory implementation of {@link InboxWriter} intended for local testing
 * and development purposes.
 *
 * <p>
 * Payloads are stored in a {@link HashMap} keyed by their idempotency key.
 * Writing the same key twice silently replaces the existing value. The inbox
 * contents can be inspected by calling {@link #toString()}.
 * </p>
 *
 * <p>
 * <strong>Note:</strong> this implementation is not thread-safe and should
 * not be used in production environments.
 * </p>
 */
public class InMemoryInboxWriter implements InboxWriter {
    private final Map<String, String> inbox;

    /** Creates a new empty in-memory inbox. */
    public InMemoryInboxWriter() {
        inbox = new HashMap<>();
    }

    @Override
    public String receive(String payload, String idempotencyKey) throws SpoolException {
        this.inbox.put(idempotencyKey, payload);
        return idempotencyKey;
    }

    @Override
    public String toString() {
        return "InMemoryInbox{" +
                buildString() +
                '}';
    }

    private String buildString() {
        StringBuilder builder = new StringBuilder();
        inbox.forEach((key, value) -> {
            builder.append(key);
            builder.append(": ");
            builder.append(value);
            builder.append("\n");
        });
        return builder.toString();
    }
}
