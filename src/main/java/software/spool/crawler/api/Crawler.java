package software.spool.crawler.api;

import software.spool.core.utils.ErrorRouter;
import software.spool.crawler.api.strategy.CrawlerStrategy;
import software.spool.crawler.internal.control.CancellationToken;

public class Crawler implements AutoCloseable {
    private final CrawlerStrategy strategy;
    private volatile CancellationToken token;
    private final ErrorRouter errorRouter;

    public Crawler(CrawlerStrategy strategy, ErrorRouter errorRouter) {
        this.strategy = strategy;
        this.token = CancellationToken.NONE;
        this.errorRouter = errorRouter;
    }

    public void startCrawling() {
        if (token.isActive()) return;
        token = CancellationToken.create();
        try {
            strategy.execute(token);
        } catch (Exception e) { errorRouter.dispatch(e); }
    }

    public void stopCrawling() {
        if (!token.isActive()) return;
        token.cancel();
        token = CancellationToken.NONE;
    }

    @Override
    public void close() throws Exception {
        token.cancel();
        token = CancellationToken.NONE;
    }
}
