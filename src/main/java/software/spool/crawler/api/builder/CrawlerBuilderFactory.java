package software.spool.crawler.api.builder;

import software.spool.core.adapter.watchdog.HttpWatchdogClient;
import software.spool.core.model.watchdog.ModuleIdentity;
import software.spool.core.port.watchdog.ModuleHeartBeat;
import software.spool.core.port.watchdog.WatchdogClient;
import software.spool.core.utils.polling.PollingHeartbeat;
import software.spool.crawler.api.port.source.PollSource;
import software.spool.crawler.api.adapter.InMemoryInboxWriter;

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
public final class CrawlerBuilderFactory {
    private CrawlerBuilderFactory() {
    }

    /**
     * Starts constructing a poll-based crawler for the given source.
     *
     * <p>
     * The returned {@link PollingCrawlerBuilder} allows further configuration of the
     * processing format, ports, and sender name before calling
     * {@link software.spool.crawler.api.strategy.CrawlerStrategy}.
     * </p>
     *
     * @param <R>    the raw type produced by the source on each poll
     * @param source the poll source to crawl; must not be {@code null}
     * @return a fluent builder step for completing the crawler configuration
     */
    public static <R> PollingCrawlerBuilder<R> poll(PollSource<R> source) {
        return new PollingCrawlerBuilder<>(source, initializeHeartbeat());
    }

    private static ModuleHeartBeat initializeHeartbeat() {
        return new PollingHeartbeat(createWatchdogClient(), ModuleIdentity.random("crawler"));
    }

    private static WatchdogClient createWatchdogClient() {
        return new HttpWatchdogClient("http://localhost:8090");
    }
}
