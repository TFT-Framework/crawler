package software.spool.crawler.internal.port.decorator;

import software.spool.core.exception.InboxWriteException;
import software.spool.core.exception.SpoolException;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.InboxItem;
import software.spool.crawler.api.port.InboxWriter;

/**
 * Decorator for {@link InboxWriter} that normalises unchecked exceptions into
 * typed {@link InboxWriteException} instances.
 *
 * <p>
 * {@link SpoolException} subclass, it is re-thrown as-is. Any other
 * {@link Exception} is wrapped in a new {@link InboxWriteException}.
 * </p>
 */
public class SafeInboxWriter implements InboxWriter {
    private final InboxWriter inbox;

    private SafeInboxWriter(InboxWriter inbox) {
        this.inbox = inbox;
    }

    /**
     * Creates a new {@code SafeInboxWriter} wrapping the given delegate.
     *
     * @param inbox the inbox writer to wrap; must not be {@code null}
     * @return a new {@code SafeInboxWriter} instance
     */
    public static SafeInboxWriter of(InboxWriter inbox) {
        return new SafeInboxWriter(inbox);
    }

    @Override
    public IdempotencyKey receive(InboxItem item) throws InboxWriteException {
        try {
            return inbox.receive(item);
        } catch (SpoolException e) {
            throw e;
        } catch (Exception e) {
            throw new InboxWriteException(e.getMessage(), e);
        }
    }
}
