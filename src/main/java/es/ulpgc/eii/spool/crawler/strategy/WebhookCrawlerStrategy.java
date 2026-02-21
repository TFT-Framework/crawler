package es.ulpgc.eii.spool.crawler.strategy;

import es.ulpgc.eii.spool.Event;
import es.ulpgc.eii.spool.EventBuffer;
import es.ulpgc.eii.spool.crawler.EventDeserializer;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A {@link CrawlerStrategy} specialized for webhook-based ingestion,
 * where external systems push raw payloads to your application.
 *
 * <p>{@code WebhookCrawlerStrategy} is typically used behind an HTTP controller
 * or any push endpoint. Incoming requests provide a raw payload of type {@code R},
 * which is deserialized into a typed {@link Event} and buffered internally
 * for later consumption through {@link #crawl()}.</p>
 *
 * <p>This design mirrors common reliable-webhook patterns: accept the HTTP
 * request quickly, enqueue the event, and process it asynchronously. [web:21]</p>
 *
 * @param <R> the raw payload type received from the webhook (e.g. {@code String})
 * @param <T> the target {@link Event} type produced after deserialization
 * @see EventDeserializer
 * @see EventBuffer
 * @since 1.0.0
 */
public interface WebhookCrawlerStrategy<R, T extends Event> extends CrawlerStrategy<T> {

    /**
     * Receives a single raw payload from the webhook entry point.
     *
     * <p>Typical usage from an HTTP controller:</p>
     * <pre>{@code
     * @PostMapping("/webhook/parking")
     * public ResponseEntity<Void> receive(@RequestBody String payload) {
     *     strategy.receive(payload);
     *     return ResponseEntity.accepted().build();
     * }
     * }</pre>
     *
     * <p>Implementations produced by {@link WebhookBuilder} will deserialize
     * the payload, push the resulting event into an {@link EventBuffer}, and
     * invoke the configured {@code onEvent} callback.</p>
     *
     * @param raw the raw payload received from the webhook; must not be {@code null}
     */
    void receive(R raw);

    /**
     * Entry point of the fluent builder for constructing a {@code WebhookCrawlerStrategy}.
     *
     * <p>The {@code rawType} parameter is currently not used at runtime but
     * documents the expected raw type at the call site:</p>
     * <pre>{@code
     * WebhookCrawlerStrategy<String, ParkingEvent> strategy =
     *         WebhookCrawlerStrategy.from(String.class)
     *                               .deserializeWith(myDeserializer)
     *                               .build();
     * }</pre>
     *
     * @param <R>     the raw payload type
     * @param rawType the raw class type (for readability only)
     * @return a {@link WebhookSourceStep} to continue the builder chain
     */
    static <R> WebhookSourceStep<R> from(Class<R> rawType) {
        return new WebhookSourceStep<>();
    }

    /**
     * Initial builder step which captures the raw payload type and
     * awaits the {@link EventDeserializer}.
     *
     * @param <R> the raw payload type
     */
    class WebhookSourceStep<R> {

        /**
         * Supplies the deserializer and advances the builder to {@link WebhookBuilder}.
         *
         * @param <T>          the target {@link Event} type
         * @param deserializer the {@link EventDeserializer} used to convert
         *                     raw payloads into typed events; must not be {@code null}
         * @return a {@link WebhookBuilder} to configure callbacks and build the strategy
         */
        public <T extends Event> WebhookBuilder<R, T> deserializeWith(EventDeserializer<R, T> deserializer) {
            return new WebhookBuilder<>(deserializer);
        }
    }

    /**
     * Final builder step that assembles the {@code WebhookCrawlerStrategy}
     * with an optional {@code onEvent} callback.
     *
     * <p>Events are stored in an internal {@link EventBuffer} created via
     * {@link EventBuffer#initialize()}. Each call to {@link #crawl()} drains
     * that buffer and returns the accumulated events as a stream.</p>
     *
     * @param <R> the raw payload type
     * @param <T> the target {@link Event} type
     */
    class WebhookBuilder<R, T extends Event> {

        private final EventDeserializer<R, T> deserializer;
        private Consumer<T> onEvent = event -> {};

        /**
         * Creates a new {@code WebhookBuilder} with the given deserializer.
         *
         * @param deserializer the deserializer from raw payloads to events;
         *                     must not be {@code null}
         */
        private WebhookBuilder(EventDeserializer<R, T> deserializer) {
            this.deserializer = deserializer;
        }

        /**
         * Registers a callback invoked immediately after each event is
         * deserialized and added to the buffer.
         *
         * <p>Useful for logging, metrics, or performing lightweight side
         * effects directly on webhook reception.</p>
         *
         * @param onEvent a non-null {@link Consumer} receiving each event
         * @return this builder, for chaining
         */
        public WebhookBuilder<R, T> onEvent(Consumer<T> onEvent) {
            this.onEvent = onEvent;
            return this;
        }

        /**
         * Builds and returns a fully configured {@code WebhookCrawlerStrategy}.
         *
         * <p>The returned strategy:</p>
         * <ul>
         *   <li>Deserializes each {@link #receive(Object) receive}d payload.</li>
         *   <li>Pushes the resulting event into an {@link EventBuffer}.</li>
         *   <li>Invokes the configured {@code onEvent} callback.</li>
         *   <li>Exposes buffered events via {@link #crawl()}.</li>
         * </ul>
         *
         * @return a ready-to-use {@code WebhookCrawlerStrategy<R, T>}
         */
        public WebhookCrawlerStrategy<R, T> build() {
            EventBuffer<T> buffer = EventBuffer.initialize();
            Consumer<T> handler = event -> { buffer.push(event); onEvent.accept(event); };
            return new WebhookCrawlerStrategy<R, T>() {
                @Override public void receive(R raw) { handler.accept(deserializer.deserialize(raw)); }
                @Override public Stream<T> crawl()   { return buffer.drain(); }
            };
        }
    }
}
