package es.ulpgc.eii.spool.crawler.strategy;

import es.ulpgc.eii.spool.Event;

import java.util.stream.Stream;

/**
 * Root abstraction for all event crawling strategies in the spool-crawler framework.
 *
 * <p>A {@code CrawlerStrategy} encapsulates the full ingestion pipeline for a
 * given transport and delivery model. All concrete strategies — Pull, Stream,
 * and Webhook — extend this interface, ensuring that the consumer always
 * interacts with events through a uniform {@link #crawl()} method regardless
 * of the underlying source.</p>
 *
 * <p>The three built-in strategies cover the most common ingestion patterns:</p>
 * <ul>
 *   <li>{@link PullCrawlerStrategy} — on-demand polling (REST API, JDBC, file)</li>
 *   <li>{@link StreamCrawlerStrategy} — continuous push stream (Kafka, WebSocket)</li>
 *   <li>{@link WebhookCrawlerStrategy} — external HTTP push notifications</li>
 * </ul>
 *
 * <p>Example — consuming events independently of the strategy implementation:</p>
 * <pre>{@code
 * CrawlerStrategy<ParkingEvent> strategy = buildStrategy(); // pull, stream or webhook
 *
 * strategy.crawl().forEach(event -> process(event));
 * }</pre>
 *
 * @param <T> the type of {@link Event} produced by this strategy
 * @see PullCrawlerStrategy
 * @see StreamCrawlerStrategy
 * @see WebhookCrawlerStrategy
 * @since 1.0.0
 */
public interface CrawlerStrategy<T extends Event> {

    /**
     * Collects and returns all events currently available from the source.
     *
     * <p>The semantics of "currently available" depend on the strategy:</p>
     * <ul>
     *   <li><b>Pull</b> — fetches and deserializes fresh data from the source on each call.</li>
     *   <li><b>Stream</b> — drains the internal {@link es.ulpgc.eii.spool.EventBuffer}
     *       accumulated since the last call to {@code crawl()}.</li>
     *   <li><b>Webhook</b> — drains the internal {@link es.ulpgc.eii.spool.EventBuffer}
     *       populated by incoming {@code receive()} calls.</li>
     * </ul>
     *
     * <p>The returned stream is sequential and consumed only once.
     * Calling {@code crawl()} again will return a new stream with any
     * subsequently available events.</p>
     *
     * @return a non-null {@link Stream} of typed events; may be empty
     */
    Stream<T> crawl();
}
