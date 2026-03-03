package software.spool.crawler.api.source;

import software.spool.core.model.SourceItemCaptured;
import software.spool.crawler.internal.port.Source;
import software.spool.core.exception.SpoolException;

import java.util.function.Consumer;

/**
 * A data source that pushes records continuously via a callback-based streaming
 * API.
 *
 * <p>
 * Unlike {@link PollSource}, the crawler does not control the frequency of
 * data arrival. Instead, the source itself drives the flow by calling the
 * {@code onMessage} consumer whenever a new item is available and
 * {@code onError} when a non-fatal error occurs.
 * </p>
 *
 * <p>
 * The source must be started with {@link #start(Consumer, Consumer)} and
 * can be gracefully stopped with {@link #stop()}.
 * </p>
 *
 * @param <R> the raw message type delivered to {@code onMessage}
 */
public interface StreamSource<R> extends Source {
    /**
     * Starts the stream and begins delivering messages to the provided callbacks.
     *
     * <p>
     * Implementations should set up any underlying connection (e.g. subscribe
     * to a Kafka topic, open a WebSocket) and start dispatching to
     * {@code onMessage} as records arrive.
     * </p>
     *
     * @param onMessage callback invoked for each captured source item;
     *                  must not be {@code null}
     * @param onError   callback invoked for non-fatal errors during streaming;
     *                  must not be {@code null}
     * @throws SpoolException if the stream could not be started
     */
    void start(Consumer<SourceItemCaptured> onMessage, Consumer<Exception> onError) throws SpoolException;

    /**
     * Stops the stream and releases any underlying resources.
     *
     * <p>
     * After this call, the {@code onMessage} and {@code onError} callbacks
     * registered in {@link #start} must no longer be invoked.
     * </p>
     */
    void stop();
}
