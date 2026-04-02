package software.spool.crawler.api.builder;

import software.spool.core.port.bus.Handler;
import software.spool.core.port.serde.NamingConvention;
import software.spool.core.port.watchdog.ModuleHeartBeat;
import software.spool.core.utils.polling.PollingConfiguration;
import software.spool.core.utils.routing.ErrorRouter;
import software.spool.crawler.api.Crawler;
import software.spool.crawler.api.port.source.PollSource;
import software.spool.crawler.api.utils.CrawlerPorts;
import software.spool.crawler.api.utils.TransformerFormat;
import software.spool.crawler.internal.control.ItemCapturedHandler;
import software.spool.crawler.internal.port.decorator.SafePollSource;
import software.spool.crawler.internal.strategy.PollingCrawlerStrategy;
import software.spool.crawler.internal.utils.factory.Transformer;

import java.time.Duration;
import java.util.Objects;

public class PollingCrawlerBuilder<I> {
    private final PollSource<I> source;
    private final ModuleHeartBeat heartBeat;
    private CrawlerPorts ports;
    private EventMappingSpecification eventMapping;
    private PollingConfiguration schedule;
    private ErrorRouter errorRouter;

    public PollingCrawlerBuilder(PollSource<I> source, ModuleHeartBeat heartBeat) {
        this.source = SafePollSource.of(source);
        this.heartBeat = heartBeat;
        this.schedule = PollingConfiguration.every(Duration.ofSeconds(30));
        this.eventMapping = new EventMappingSpecification(NamingConvention.SNAKE_CASE);
    }

    public PollingCrawlerBuilder<I> ports(CrawlerPorts ports) {
        this.ports = ports;
        return this;
    }

    public PollingCrawlerBuilder<I> eventMapping(EventMappingSpecification eventMapping) {
        this.eventMapping = eventMapping;
        return this;
    }

    public PollingCrawlerBuilder<I> schedule(PollingConfiguration config) {
        this.schedule = config;
        return this;
    }

    public PollingCrawlerBuilder<I> withErrorRouter(ErrorRouter errorRouter) {
        this.errorRouter = errorRouter;
        return this;
    }

    public <T, O> Crawler createWith(Transformer<T, O> transformer) {
        validateRequiredFields();
        if (eventMapping.hasConflict())
            throw new IllegalArgumentException("Only one can be used at the same time. Please, use addDomainEvent(...) or addPartitionAttributes(...) but not both.");
        return new Crawler(initializeStrategy(transformer, initializeHandler()), errorRouter, heartBeat);
    }

    private <T, O> PollingCrawlerStrategy<I, T, O> initializeStrategy(Transformer<T, O> transformer, Handler<String> handler) {
        return new PollingCrawlerStrategy<>(source, transformer, handler, schedule, errorRouter);
    }

    private ItemCapturedHandler initializeHandler() {
        return new ItemCapturedHandler(
                source.sourceId(), ports,
                eventMapping.buildEmitter(ports.bus()),
                eventMapping.partitionAttributes(),
                errorRouter);
    }

    public <T, O> Crawler createWith(TransformerFormat<T, O> format) {
        return createWith(format.pipeline());
    }

    private void validateRequiredFields() {
        Objects.requireNonNull(ports, "CrawlerPorts must be set before calling create()");
    }
}