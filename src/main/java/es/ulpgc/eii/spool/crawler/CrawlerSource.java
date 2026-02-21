package es.ulpgc.eii.spool.crawler;

import java.util.stream.Stream;

/**
 * Abstraction over any pull-based data source from which raw events can be read.
 *
 * <p>{@code CrawlerSource} decouples the transport mechanism (REST API, JDBC,
 * file system, etc.) from the deserialization and processing logic. Implementations
 * are responsible solely for fetching raw payloads and exposing them as a
 * {@link Stream}.</p>
 *
 * <p>A typical implementation might wrap an HTTP client:</p>
 * <pre>{@code
 * CrawlerSource<String> apiSource = () ->
 *         httpClient.getLines("/api/parking/occupancy");
 * }</pre>
 *
 * <p>Or a JDBC query:</p>
 * <pre>{@code
 * CrawlerSource<ResultSet> dbSource = () ->
 *         Stream.of(statement.executeQuery("SELECT * FROM occupancy"));
 * }</pre>
 *
 * @param <R> the type of the raw payload returned by this source
 *            (e.g. {@code String} for JSON, {@code ResultSet} for JDBC)
 * @see es.ulpgc.eii.spool.crawler.strategy.PullCrawlerStrategy
 * @since 1.0.0
 */
public interface CrawlerSource<R> {

    /**
     * Reads all currently available raw payloads from the source.
     *
     * <p>Each element in the returned stream represents a single raw event
     * payload that will later be deserialized into a typed {@link es.ulpgc.eii.spool.Event}.
     * The stream may be empty if no new data is available.</p>
     *
     * <p>Implementations should not perform deserialization or business logic —
     * that responsibility belongs to the {@link es.ulpgc.eii.spool.crawler.EventDeserializer}.</p>
     *
     * @return a non-null {@link Stream} of raw payloads; may be empty
     */
    Stream<R> read();
}
