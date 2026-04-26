package software.spool.crawler.internal.strategy;

import software.spool.core.exception.*;
import software.spool.core.port.bus.Handler;
import software.spool.core.utils.polling.CancellationToken;
import software.spool.core.utils.polling.PollingConfiguration;
import software.spool.core.utils.routing.ErrorRouter;
import software.spool.crawler.api.strategy.CrawlerStrategy;
import software.spool.crawler.internal.utils.factory.Normalizer;
import software.spool.crawler.api.port.source.PollSource;

import java.time.Duration;
import java.util.Objects;

public class PollingCrawlerStrategy<I, P, E, R> implements CrawlerStrategy {
    private final PollSource<I> source;
    private final Normalizer<P, E, R> normalizer;
    private final ErrorRouter errorRouter;
    private final Handler<String> itemmCapturedHandler;
    private final PollingConfiguration pollingConfiguration;

    public PollingCrawlerStrategy(PollSource<I> source, Normalizer<P, E, R> normalizer,
                                  Handler<String> itemmCapturedHandler,
                                  PollingConfiguration pollingConfiguration, ErrorRouter errorRouter) {
        this.source = Objects.requireNonNull(source);
        this.normalizer = Objects.requireNonNull(normalizer);
        this.errorRouter = Objects.requireNonNull(errorRouter);
        this.itemmCapturedHandler = Objects.requireNonNull(itemmCapturedHandler);
        this.pollingConfiguration = Objects.requireNonNullElse(pollingConfiguration,
                PollingConfiguration.every(Duration.ofSeconds(10)));
    }

    @Override
    public void execute(CancellationToken token) throws SpoolException {
        pollingConfiguration.scheduler().schedule(
                () -> { try (PollSource<I> openedSource = this.source.open()) {
                        normalizer.transform(openedSource.poll())
                            .takeWhile(p -> token.isActive())
                            .forEach(itemmCapturedHandler::handle);
                    } catch (Exception e) { errorRouter.dispatch(e); }
                },
                pollingConfiguration.policy(),
                token
        );
    }
}
