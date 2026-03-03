package software.spool.crawler.api;

import software.spool.core.model.SpoolEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Routes exceptions to type-specific handlers in the order they were
 * registered.
 *
 * <p>
 * {@code ErrorRouter} implements a chain-of-responsibility pattern: when
 * {@link #dispatch(Exception, SpoolEvent)} is called, it walks through the
 * registered handlers in insertion order and delegates to the first one whose
 * declared exception type matches the thrown exception. If no handler matches,
 * the optional fallback consumer is invoked instead.
 * </p>
 *
 * <p>
 * Handlers are registered fluently:
 * </p>
 * 
 * <pre>{@code
 * ErrorRouter router = new ErrorRouter()
 *         .on(SourcePollException.class, (e, event) -> log.warn("Poll failed", e))
 *         .on(SerializationException.class, (e, event) -> log.error("Serialization failed", e))
 *         .orElse((e, event) -> log.error("Unexpected error", e));
 * }</pre>
 */
public class ErrorRouter {

    /** Internal pairing of exception type and its associated handler. */
    private record Entry<E extends Exception>(Class<E> type, BiConsumer<E, SpoolEvent> handler) {
        @SuppressWarnings("unchecked")
        boolean tryHandle(Exception e, SpoolEvent context) {
            if (type.isInstance(e)) {
                handler.accept((E) e, context);
                return true;
            }
            return false;
        }
    }

    /** Ordered list of registered exception handlers. */
    private final List<Entry<?>> entries = new ArrayList<>();

    /**
     * Fallback handler invoked when no registered handler matches. Does nothing by
     * default.
     */
    private BiConsumer<Exception, SpoolEvent> fallback = (e, cause) -> {
    };

    /**
     * Registers a handler for the given exception type.
     *
     * <p>
     * Handlers are evaluated in registration order; the first match wins.
     * Instance-of checks are used, so a handler registered for a supertype will
     * also match subtypes unless a more specific handler was registered earlier.
     * </p>
     *
     * @param <E>     the exception type to handle
     * @param type    the {@link Class} object representing the exception type
     * @param handler consumer that receives the exception and the optional
     *                {@link SpoolEvent} context that was active when the error
     *                occurred
     * @return this router instance for method chaining
     */
    public <E extends Exception> ErrorRouter on(Class<E> type, BiConsumer<E, SpoolEvent> handler) {
        entries.add(new Entry<>(type, handler));
        return this;
    }

    /**
     * Sets the fallback handler that is invoked when no registered handler matches
     * the thrown exception.
     *
     * @param fallback consumer called with the unmatched exception and its optional
     *                 {@link SpoolEvent} context
     * @return this router instance for method chaining
     */
    public ErrorRouter orElse(BiConsumer<Exception, SpoolEvent> fallback) {
        this.fallback = fallback;
        return this;
    }

    /**
     * Dispatches the given exception with no event context.
     *
     * <p>
     * Convenience overload; equivalent to {@code dispatch(exception, null)}.
     * </p>
     *
     * @param exception the exception to route
     */
    public void dispatch(Exception exception) {
        dispatch(exception, null);
    }

    /**
     * Dispatches the given exception, providing an optional {@link SpoolEvent} as
     * context.
     *
     * <p>
     * The first registered handler whose declared type is assignable from
     * {@code exception} is invoked. If none matches, the fallback handler is used.
     * If no fallback is set, the exception is silently ignored.
     * </p>
     *
     * @param exception the exception to route
     * @param context   the event that was being processed when the error occurred,
     *                  or {@code null} if no context is available
     */
    public void dispatch(Exception exception, SpoolEvent context) {
        entries.stream()
                .filter(entry -> entry.tryHandle(exception, context))
                .findFirst()
                .orElseGet(() -> {
                    fallback.accept(exception, context);
                    return null;
                });
    }
}
