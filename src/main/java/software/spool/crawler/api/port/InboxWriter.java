package software.spool.crawler.api.port;

import software.spool.core.exception.*;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.Envelope;

public interface InboxWriter {
    /**
     * Stores a serialized payload in the inbox, using the provided idempotency key
     * to prevent duplicate entries.
     *
     * @return the identifier assigned to the stored entry
     * @throws InboxWriteException if the entry could not be persisted
     * @throws DuplicateEventException if the entry was duplicated
     */
    IdempotencyKey receive(Envelope envelope) throws InboxWriteException, DuplicateEventException;
}
