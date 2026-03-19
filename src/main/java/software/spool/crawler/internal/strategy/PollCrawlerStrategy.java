package software.spool.crawler.internal.strategy;

import software.spool.core.control.Handler;
import software.spool.core.exception.*;
import software.spool.core.utils.ErrorRouter;
import software.spool.crawler.api.port.PayloadSplitter;
import software.spool.crawler.api.strategy.CrawlerStrategy;
import software.spool.crawler.internal.control.CancellationToken;
import software.spool.crawler.internal.utils.factory.Transformer;
import software.spool.crawler.api.port.source.PollSource;

/**
 * Concrete {@link CrawlerStrategy} that orchestrates a full
 * poll-deserialize-split-serialize
 * cycle and delivers each resulting record to the inbox and event bus.
 *
 * <ol>
 * <li>Open the {@link PollSource} inside a try-with-resources block.</li>
 * <li>Call {@link PollSource#poll()} to fetch the raw payload.</li>
 * <li>Deserialize the raw payload via the configured
 * {@link software.spool.core.port.PayloadDeserializer}.</li>
 * <li>Split the deserialized value into individual records via the configured
 * {@link PayloadSplitter}.</li>
 * <li>For each record: serialize it, write it to the inbox, and emit
 * {@code SourceItemCaptured} and {@code InboxItemStored} events.</li>
 * <li>Route any {@link SpoolException} through the
 * {@link software.spool.core.utils.ErrorRouter}
 * </ol>
 *
 * <p>
 * Idempotency keys are derived by hashing {@code sourceId + ":" + payload}
 * with SHA-256, ensuring that identical records from the same source always
 * produce the same key.
 * </p>
 *
 * @param <I> the raw type produced by the {@link PollSource}
 * @param <T> the intermediate type after deserialization
 * @param <O> the individual record type produced by the splitter
 */
public class PollCrawlerStrategy<I, T, O> implements CrawlerStrategy {
    private final PollSource<I> source;
    private final Transformer<T, O> transformer;
    private final ErrorRouter errorRouter;
    private final Handler<String> itemmCapturedHandler;

    public PollCrawlerStrategy(
            PollSource<I> source,
            Transformer<T, O> transformer,
            ErrorRouter errorRouter, Handler<String> itemmCapturedHandler) {
        this.source = source;
        this.transformer = transformer;
        this.errorRouter = errorRouter;
        this.itemmCapturedHandler = itemmCapturedHandler;
    }

    @Override
    public void execute(CancellationToken token) throws SpoolException {
        try (PollSource<I> openedSource = this.source.open()) {
            transformer.transform(openedSource.poll())
                    .takeWhile(p -> token.isActive())
                    .forEach(itemmCapturedHandler::handle);
        } catch (Exception e) { errorRouter.dispatch(e); }
    }
}
