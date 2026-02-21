package es.ulpgc.eii.spool.crawler;

/**
 * Entry point for receiving raw payloads pushed from an external source.
 *
 * <p>{@code EventInbox} represents the inbound side of a push-based ingestion
 * channel. Unlike {@link CrawlerSource}, where the framework actively pulls
 * data, an {@code EventInbox} passively waits for callers (e.g. an HTTP
 * controller, a message listener) to deliver raw payloads.</p>
 *
 * <p>Typical usage in a Spring MVC webhook endpoint:</p>
 * <pre>{@code
 * @RestController
 * public class ParkingWebhookController implements EventInbox<String> {
 *
 *     private final WebhookCrawlerStrategy<String, ParkingEvent> strategy;
 *
 *     @PostMapping("/webhook/parking")
 *     public ResponseEntity<Void> receive(@RequestBody String payload) {
 *         strategy.receive(payload);
 *         return ResponseEntity.ok().build();
 *     }
 * }
 * }</pre>
 *
 * @param <R> the type of the raw payload this inbox accepts
 *            (e.g. {@code String} for JSON, {@code byte[]} for binary)
 * @see es.ulpgc.eii.spool.crawler.strategy.WebhookCrawlerStrategy
 * @since 1.0.0
 */
@FunctionalInterface
public interface EventInbox<R> {

    /**
     * Receives a single raw payload and hands it off for processing.
     *
     * <p>Implementations should be non-blocking and delegate any
     * deserialization or business logic to downstream components
     * (e.g. an {@link EventDeserializer}). This keeps the inbox
     * focused solely on accepting and forwarding the raw input.</p>
     *
     * @param event the raw payload to receive; must not be {@code null}
     */
    void receive(R event);
}
