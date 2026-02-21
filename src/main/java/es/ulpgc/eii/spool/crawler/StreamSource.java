package es.ulpgc.eii.spool.crawler;

import java.util.function.Consumer;

/**
 * Abstraction over any continuous, push-based event source such as
 * Kafka, RabbitMQ, WebSockets, or Server-Sent Events.
 *
 * <p>{@code StreamSource} manages the lifecycle of a long-lived connection
 * to the underlying transport. Once started, it pushes each incoming raw
 * payload to the provided {@code onMessage} callback; errors are forwarded
 * to {@code onError} without interrupting the stream unless the
 * implementation decides otherwise.</p>
 *
 * <p>Example — wrapping a Kafka consumer:</p>
 * <pre>{@code
 * public class KafkaStreamSource implements StreamSource<String> {
 *
 *     private final KafkaConsumer<String, String> consumer;
 *     private volatile boolean running;
 *
 *     @Override
 *     public void start(Consumer<String> onMessage, Consumer<Exception> onError) {
 *         running = true;
 *         while (running) {
 *             consumer.poll(Duration.ofMillis(100))
 *                     .forEach(record -> onMessage.accept(record.value()));
 *         }
 *     }
 *
 *     @Override
 *     public void stop() { running = false; }
 * }
 * }</pre>
 *
 * @param <R> the type of the raw payload emitted by this source
 *            (e.g. {@code String} for JSON, {@code byte[]} for Avro)
 * @see es.ulpgc.eii.spool.crawler.strategy.StreamCrawlerStrategy
 * @since 1.0.0
 */
public interface StreamSource<R> {

    /**
     * Starts the source and begins delivering incoming payloads to the
     * provided callbacks.
     *
     * <p>This method may block the calling thread (e.g. a Kafka poll loop)
     * or return immediately and push messages asynchronously, depending on
     * the implementation. Callers should invoke this on a dedicated thread
     * if blocking behaviour is expected.</p>
     *
     * @param onMessage callback invoked for each successfully received raw
     *                  payload; must not be {@code null}
     * @param onError   callback invoked when an error occurs while receiving
     *                  or processing a message; must not be {@code null}.
     *                  The source should continue running after a non-fatal
     *                  error unless the implementation explicitly stops it
     */
    void start(Consumer<R> onMessage, Consumer<Exception> onError);

    /**
     * Signals the source to stop delivering messages and release any
     * underlying resources (connections, threads, file handles, etc.).
     *
     * <p>After {@code stop()} returns, no further calls to {@code onMessage}
     * or {@code onError} should be made. Implementations should aim to
     * honour this contract in a timely, graceful manner.</p>
     */
    void stop();
}
