package software.spool.crawler.api.port;

import software.spool.core.exception.InboxWriteException;
import software.spool.core.model.SourceItemCaptured;

/**
 * Port for writing captured payloads into the inbox.
 *
 * <p>
 * Implement this interface to connect the crawler to your storage backend
 * (database, message queue, file system, etc.). The crawler calls
 * {@link #receive(String, String)} once per processed record.
 * </p>
 *
 * <p>
 * A simple in-memory implementation for testing/local use is available in
 * {@link software.spool.crawler.internal.utils.InMemoryInboxWriter}.
 * </p>
 */
public interface InboxWriter {
    /**
     * Stores a serialized payload in the inbox, using the provided idempotency key
     * to prevent duplicate entries.
     *
     * @param payload        the serialized record to store (never {@code null})
     * @param idempotencyKey a deterministic key that uniquely identifies this
     *                       record across repeated invocations; implementations
     *                       should ignore or update existing entries with the same
     *                       key
     * @return the identifier assigned to the stored entry
     * @throws InboxWriteException if the entry could not be persisted
     */
    String receive(String payload, String idempotencyKey) throws InboxWriteException;
}
