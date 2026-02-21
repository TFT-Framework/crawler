package es.ulpgc.eii.spool.crawler.strategy;

import es.ulpgc.eii.spool.Event;
import es.ulpgc.eii.spool.crawler.CrawlerSource;
import es.ulpgc.eii.spool.crawler.EventDeserializer;

import java.util.stream.Stream;

/**
 * A {@link CrawlerStrategy} for on-demand, pull-based event ingestion.
 *
 * <p>{@code PullCrawlerStrategy} is the right choice when the framework
 * needs to actively request data from a source on each cycle — for example,
 * polling a REST API, querying a database, or reading a file. The source
 * is only contacted when {@link #crawl()} (or {@link #fetch()}) is called.</p>
 *
 * <p>Instances are built through the fluent factory API:</p>
 * <pre>{@code
 * PullCrawlerStrategy<ParkingEvent> strategy =
 *         PullCrawlerStrategy.from(apiSource)
 *                            .deserializeWith(raw -> JsonMapper.parse(raw, ParkingEvent.class));
 *
 * strategy.crawl().forEach(event -> process(event));
 * }</pre>
 *
 * @param <T> the type of {@link Event} produced by this strategy
 * @see CrawlerSource
 * @see EventDeserializer
 * @see StreamCrawlerStrategy
 * @since 1.0.0
 */
public interface PullCrawlerStrategy<T extends Event> extends CrawlerStrategy<T> {

    /**
     * Fetches and deserializes all currently available events from the source.
     *
     * <p>This is the core operation of the pull strategy. Each call triggers
     * a fresh read from the underlying {@link CrawlerSource}, so the returned
     * stream reflects the state of the source at the moment of the call.</p>
     *
     * @return a non-null {@link Stream} of typed events; may be empty if the
     *         source has no new data available
     */
    Stream<T> fetch();

    /**
     * Delegates to {@link #fetch()}, fulfilling the {@link CrawlerStrategy} contract.
     *
     * <p>Pull strategies do not maintain an internal buffer — every {@code crawl()}
     * call results in a live read from the source.</p>
     *
     * @return the result of {@link #fetch()}
     */
    @Override
    default Stream<T> crawl() {
        return fetch();
    }

    /**
     * Entry point of the fluent builder for constructing a {@code PullCrawlerStrategy}.
     *
     * <p>Usage:</p>
     * <pre>{@code
     * PullCrawlerStrategy<ParkingEvent> strategy =
     *         PullCrawlerStrategy.from(mySource)
     *                            .deserializeWith(myDeserializer);
     * }</pre>
     *
     * @param <R>    the type of raw payload provided by the source
     * @param source the {@link CrawlerSource} to read raw payloads from;
     *               must not be {@code null}
     * @return a {@link SourceStep} to continue the builder chain
     */
    static <R> SourceStep<R> from(CrawlerSource<R> source) {
        return new SourceStep<>(source);
    }

    /**
     * Intermediate builder step that holds the configured {@link CrawlerSource}
     * and awaits the {@link EventDeserializer} to complete the strategy.
     *
     * @param <R> the type of raw payload produced by the source
     */
    class SourceStep<R> {

        private final CrawlerSource<R> source;

        /**
         * Creates a new {@code SourceStep} wrapping the given source.
         *
         * @param source the source to wrap; must not be {@code null}
         */
        private SourceStep(CrawlerSource<R> source) {
            this.source = source;
        }

        /**
         * Completes the builder by supplying the deserializer and returns a
         * ready-to-use {@code PullCrawlerStrategy}.
         *
         * <p>The resulting strategy, when {@link PullCrawlerStrategy#crawl() crawl()}
         * is called, will read raw payloads from the source and map each one through
         * the provided deserializer.</p>
         *
         * @param <T>          the target {@link Event} type
         * @param deserializer the {@link EventDeserializer} used to convert each
         *                     raw payload into a typed event; must not be {@code null}
         * @return a fully configured {@code PullCrawlerStrategy<T>}
         */
        public <T extends Event> PullCrawlerStrategy<T> deserializeWith(EventDeserializer<R, T> deserializer) {
            return () -> source.read().map(deserializer::deserialize);
        }
    }
}
