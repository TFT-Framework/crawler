package software.spool.crawler.api;

import software.spool.core.model.spool.SpoolModule;
import software.spool.core.model.spool.SpoolNode;
import software.spool.core.port.health.ModuleHealthPayload;
import software.spool.core.port.watchdog.ModuleHeartBeat;
import software.spool.core.utils.polling.CancellationToken;
import software.spool.core.utils.routing.ErrorRouter;
import software.spool.crawler.api.strategy.CrawlerStrategy;

import java.util.Objects;

public class Crawler implements SpoolModule {
    private final CrawlerStrategy strategy;
    private volatile CancellationToken token;
    private final ErrorRouter errorRouter;
    private final ModuleHeartBeat heartBeat;

    public Crawler(CrawlerStrategy strategy, ErrorRouter errorRouter, ModuleHeartBeat heartBeat) {
        this.strategy = strategy;
        this.errorRouter = errorRouter;
        this.heartBeat = heartBeat;
        this.token = CancellationToken.NOOP;
    }

    @Override
    public void start(SpoolNode.StartPermit permit) {
        if (token.isActive()) return;
        Objects.requireNonNull(permit);
        token = CancellationToken.create();
        try {
            heartBeat.start();
            strategy.execute(token);
        } catch (Exception e) { errorRouter.dispatch(e); }
    }

    @Override
    public void stop(SpoolNode.StartPermit permit) {
        if (!token.isActive()) return;
        Objects.requireNonNull(permit);
        token.cancel();
        heartBeat.stop();
        token = CancellationToken.NOOP;
    }

    @Override
    public ModuleHealthPayload checkHealth() {
        return token.isActive() ? ModuleHealthPayload.healthy(heartBeat.identity().moduleId()) : ModuleHealthPayload.degraded(heartBeat.identity().moduleId(), null);
    }
}
