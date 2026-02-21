package es.ulpgc.eii.spool.crawler.strategy;

import es.ulpgc.eii.spool.Event;
import es.ulpgc.eii.spool.EventBuffer;
import es.ulpgc.eii.spool.crawler.EventDeserializer;
import es.ulpgc.eii.spool.crawler.StreamSource;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A {@link CrawlerStrategy} for continuous, push-based event ingestion from
 * long-lived streaming sources such as Kafka, RabbitMQ, or WebSockets.
 *
 * <p>Unlike {@link PullCrawlerStrategy}, a {@code StreamCrawlerStrategy} maintains
 * an active connection to the source. Incoming raw payloads are deserialized and
 * accumulated in an internal {@link EventBuffer} as they arrive. Calling
 * {@link #crawl()} drains that buffer and returns a snapshot of all events
 * collected since the previous drain.</p>
 *
 * <p>Instances are built through the fluent factory API:</p>
 * <pre>{@code
 * StreamCrawlerStrategy<ParkingEvent> strategy =
 *         StreamCrawlerStrategy.from(kafkaSource)
 *                              .deserializeWith(raw -> JsonMapper.parse(raw, ParkingEvent.class))
 *                              .onEvent(event -> log.info("Received: {}", event.eventType()))
 *                              .onError(ex   -> log.error("Stream error", ex))
 *                              .build();
 *
 * strategy.start(); // begins listening in the background
 *
 * // Later, in a scheduled task:
 * strategy.crawl().forEach(event -> process(event));
 *
 * strategy.stop(); // gracefully shuts down the connection
 * }</pre>
 *
 * @param <T> the type of {@link Event} produced by this strategy
 * @see StreamSource
 * @see EventDeserializer
 * @see EventBuffer
 * @since 1.0.0
 */
public interface StreamCrawlerStrategy<T extends Event> extends CrawlerStrategy<T> {

    /**
     * Starts the underlying {@link StreamSource} and begins receiving events.
     *
     * <p>Depending on the {@link StreamSource} implementation, this method may
     * block the calling thread (e.g. a Kafka poll loop) or return immediately
     * and push messages asynchronously. Callers should invoke this on a
     * dedicated thread if blocking behaviour is expected.</p>
     *
     * <p>Each incoming raw payload is deserialized and pushed into the internal
     * {@link EventBuffer}, making it available on the next {@link #crawl()} call.</p>
     */
    void start();

    /**
     * Stops the underlying {@link StreamSource} and releases its resources.
     *
     * <p>After {@code stop()} returns, no further events will be added to the
     * internal buffer. Any events already buffered remain available and can
     * still be retrieved via {@link #crawl()}.</p>
     */
    void stop();

    /**
     * Entry point of the fluent builder for constructing a {@code StreamCrawlerStrategy}.
     *
     * <pre>{@code
     * StreamCrawlerStrategy<ParkingEvent> strategy =
     *         StreamCrawlerStrategy.from(kafkaSource)
     *                              .deserializeWith(myDeserializer)
     *                              .build();
     * }</pre>
     *
     * @param <R>    the type of raw payload emitted by the source
     * @param source the {@link StreamSource} to connect to; must not be {@code null}
     * @return a {@link SourceStep} to continue the builder chain
     */
    static <R> SourceStep<R> from(StreamSource<R> source) {
        return new SourceStep<>(source);
    }

    /**
     * Intermediate builder step that holds the configured {@link StreamSource}
     * and awaits the {@link EventDeserializer} to proceed.
     *
     * @param <R> the type of raw payload produced by the source
     */
    class SourceStep<R> {

        private final StreamSource<R> source;

        /**
         * Creates a new {@code SourceStep} wrapping the given source.
         *
         * @param source the stream source to wrap; must not be {@code null}
         */
        private SourceStep(StreamSource<R> source) {
            this.source = source;
        }

        /**
         * Supplies the deserializer and advances the builder to {@link StreamBuilder}.
         *
         * @param <T>          the target {@link Event} type
         * @param deserializer the {@link EventDeserializer} that maps raw payloads
         *                     to typed events; must not be {@code null}
         * @return a {@link StreamBuilder} to configure optional callbacks and build
         *         the final strategy
         */
        public <T extends Event> StreamBuilder<R, T> deserializeWith(EventDeserializer<R, T> deserializer) {
            return new StreamBuilder<>(source, deserializer);
        }
    }

    /**
     * Final builder step that assembles the {@code StreamCrawlerStrategy} with
     * optional event and error callbacks.
     *
     * <p>Defaults:</p>
     * <ul>
     *   <li>{@code onEvent} — no-op; events are buffered silently.</li>
     *   <li>{@code onError} — wraps the exception in a {@link RuntimeException}
     *       and rethrows it.</li>
     * </ul>
     *
     * @param <R> the type of raw payload produced by the source
     * @param <T> the target {@link Event} type
     */
    class StreamBuilder<R, T extends Event> {

        private final StreamSource<R>        source;
        private final EventDeserializer<R,T> deserializer;
        private Consumer<T>         onEvent = event -> {};
        private Consumer<Exception> onError = error -> { throw new RuntimeException(error); };

        /**
         * Creates a new {@code StreamBuilder} with the given source and deserializer.
         *
         * @param source       the stream source; must not be {@code null}
         * @param deserializer the deserializer; must not be {@code null}
         */
        private StreamBuilder(StreamSource<R> source, EventDeserializer<R, T> deserializer) {
            this.source       = source;
            this.deserializer = deserializer;
        }

        /**
         * Registers a callback invoked immediately after each event is deserialized
         * and added to the buffer.
         *
         * <p>Useful for real-time side effects such as logging, metrics, or
         * triggering downstream notifications without waiting for the next
         * {@link #crawl()} cycle.</p>
         *
         * @param onEvent a non-null {@link Consumer} receiving each typed event
         * @return this builder, for chaining
         */
        public StreamBuilder<R, T> onEvent(Consumer<T> onEvent) {
            this.onEvent = onEvent;
            return this;
        }

        /**
         * Registers a callback invoked whenever an error occurs during message
         * reception or deserialization.
         *
         * <p>Overrides the default behaviour of rethrowing the error as a
         * {@link RuntimeException}. A common alternative is to log the error
         * and continue processing subsequent messages.</p>
         *
         * @param onError a non-null {@link Consumer} receiving the exception
         * @return this builder, for chaining
         */
        public StreamBuilder<R, T> onError(Consumer<Exception> onError) {
            this.onError = onError;
            return this;
        }

        /**
         * Builds and returns a fully configured {@code StreamCrawlerStrategy}.
         *
         * <p>The returned strategy owns an internal {@link EventBuffer} that
         * accumulates events from the moment {@link StreamCrawlerStrategy#start()}
         * is called. Each {@link StreamCrawlerStrategy#crawl()} call atomically
         * drains that buffer.</p>
         *
         * @return a ready-to-use {@code StreamCrawlerStrategy<T>}
         */
        public StreamCrawlerStrategy<T> build() {
            EventBuffer<T> buffer = new EventBuffer<>();
            Consumer<T> handler = event -> { buffer.push(event); onEvent.accept(event); };
            return new StreamCrawlerStrategy<T>() {
                @Override public void start()      { source.start(raw -> handler.accept(deserializer.deserialize(raw)), onError); }
                @Override public void stop()       { source.stop(); }
                @Override public Stream<T> crawl() { return buffer.drain(); }
            };
        }
    }
}
