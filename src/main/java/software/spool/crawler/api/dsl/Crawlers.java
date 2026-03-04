package software.spool.crawler.api.dsl;

import software.spool.crawler.api.source.PollSource;
import software.spool.crawler.internal.utils.CrawlerPorts;
import software.spool.crawler.internal.utils.InMemoryInboxWriter;

/**
 * Entry point of the crawler DSL.
 *
 * <p>
 * Use the static factory methods in this class to start building a
 * {@link software.spool.crawler.api.strategy.CrawlerStrategy} in a
 * fluent, readable way:
 * </p>
 * 
 * <pre>{@code
 * CrawlerStrategy strategy = Crawlers.poll(mySource)
 *         .withFormat(Formats.JSON_ARRAY)
 *         .inbox(myInboxWriter)
 *         .bus(myEventBus)
 *         .senderName("my-crawler")
 *         .create();
 * }</pre>
 *
 * <p>
 * The default ports injected when not explicitly configured are:
 * <ul>
 * <li><b>bus</b> – prints events to {@code System.out}</li>
 * <li><b>inbox</b> – {@link InMemoryInboxWriter} (suitable for local
 * testing)</li>
 * </ul>
 */
public final class Crawlers {
    private Crawlers() {
    }

    /**
     * Starts constructing a poll-based crawler for the given source.
     *
     * <p>
     * The returned {@link PollSourceStep} allows further configuration of the
     * processing format, ports, and sender name before calling
     * {@link PollSourceStep#create()} to obtain the final
     * {@link software.spool.crawler.api.strategy.CrawlerStrategy}.
     * </p>
     *
     * @param <R>    the raw type produced by the source on each poll
     * @param source the poll source to crawl; must not be {@code null}
     * @return a fluent builder step for completing the crawler configuration
     */
    public static <R> PollSourceStep<R, R, R> poll(PollSource<R> source) {
        return new PollSourceStep<>(source,
                CrawlerPorts.builder().bus(System.out::println).inbox(new InMemoryInboxWriter()).build());
    }
}
