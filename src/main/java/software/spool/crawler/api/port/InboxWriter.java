package software.spool.crawler.api.port;

import software.spool.core.exception.*;
import software.spool.core.model.IdempotencyKey;
import software.spool.core.model.InboxItem;
import software.spool.crawler.api.adapter.InMemoryInboxWriter;

/**
 * Port for writing captured payloads into the inbox.
 *
 * <p>
 * Implement this interface to connect the crawler to your storage backend
 * (database, message queue, file system, etc.). The crawler calls
 * </p>
 *
 * <p>
 * A simple in-memory implementation for testing/local use is available in
 * {@link InMemoryInboxWriter}.
 * </p>
 */
public interface InboxWriter {
    /**
     * Stores a serialized payload in the inbox, using the provided idempotency key
     * to prevent duplicate entries.
     *
     * @return the identifier assigned to the stored entry
     * @throws InboxWriteException if the entry could not be persisted
     * @throws DuplicateEventException if the entry was duplicated
     */
    IdempotencyKey receive(InboxItem item) throws InboxWriteException, DuplicateEventException;
}
