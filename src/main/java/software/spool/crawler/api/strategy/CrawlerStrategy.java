package software.spool.crawler.api.strategy;

import software.spool.core.exception.SpoolException;
import software.spool.crawler.api.builder.CrawlerBuilderFactory;

/**
 * Core abstraction representing a single crawler execution cycle.
 *
 * <p>
 * A {@code CrawlerStrategy} encapsulates the complete logic for one run:
 * fetching data from a source, transforming it through the configured pipeline,
 * and delivering the resulting records to the inbox and event bus.
 * </p>
 *
 * <p>
 * The typical entry point for obtaining a pre-configured strategy is the
 * fluent DSL provided by {@link CrawlerBuilderFactory}:
 * </p>
 * 
 * <pre>{@code
 * CrawlerStrategy strategy = Crawlers.poll(mySource)
 *         .withFormat(Formats.JSON_ARRAY)
 *         .inbox(myInboxWriter)
 *         .create();
 *
 * strategy.execute();
 * }</pre>
 */
public interface CrawlerStrategy {
    /**
     * Runs one complete crawler cycle.
     *
     * <p>
     * Implementations should fetch data from their source, apply the processing
     * pipeline, and route the results to the inbox and event bus. Errors are
     * expected to be handled internally via an
     * {@link software.spool.core.utils.ErrorRouter ErrorRouter}
     * rather than propagated to the caller whenever possible.
     * </p>
     *
     * @throws SpoolException if an unrecoverable error occurs that cannot be
     *                        handled by the internal error router
     */
    void execute() throws SpoolException;
}
