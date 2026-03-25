package software.spool.crawler.api.builder;

import software.spool.core.utils.ErrorRouter;
import software.spool.core.utils.NamingConvention;
import software.spool.core.utils.PollingConfiguration;
import software.spool.crawler.api.Crawler;
import software.spool.crawler.api.port.source.PollSource;
import software.spool.crawler.api.utils.CrawlerPorts;
import software.spool.crawler.api.utils.TransformerFormat;
import software.spool.crawler.internal.control.ItemCapturedHandler;
import software.spool.crawler.internal.port.decorator.SafePayloadDeserializer;
import software.spool.crawler.internal.port.decorator.SafePayloadSplitter;
import software.spool.crawler.internal.port.decorator.SafePollSource;
import software.spool.crawler.internal.port.decorator.SafeRecordSerializer;
import software.spool.crawler.internal.strategy.PollingCrawlerStrategy;
import software.spool.crawler.internal.utils.factory.Transformer;

import java.time.Duration;

public class PollSourceBuilder<I, T, O> {
    private final PollSource<I> source;
    private Transformer<T, O> transformer;
    private CrawlerPorts ports;
    private EventMappingSpecification eventMapping;
    private PollingConfiguration schedule;
    private ErrorRouter errorRouter;

    public PollSourceBuilder(PollSource<I> source) {
        this.source = SafePollSource.of(source);
        this.schedule = PollingConfiguration.every(Duration.ofSeconds(30));
        this.eventMapping = new EventMappingSpecification(NamingConvention.SNAKE_CASE);
    }

    private PollSourceBuilder(PollSource<I> source, CrawlerPorts ports, EventMappingSpecification eventMapping,
                              PollingConfiguration schedule, ErrorRouter errorRouter) {
        this.source = SafePollSource.of(source);
        this.ports = ports;
        this.eventMapping = eventMapping;
        this.schedule = schedule;
        this.errorRouter = errorRouter;
    }

    public PollSourceBuilder<I, T, O> transformer(Transformer<T, O> transformer) {
        this.transformer = Transformer.of(
                SafePayloadDeserializer.of(transformer.deserializer()),
                SafePayloadSplitter.of(transformer.splitter()),
                SafeRecordSerializer.of(transformer.serializer()));
        return this;
    }

    public PollSourceBuilder<I, T, O> ports(CrawlerPorts ports) {
        this.ports = ports;
        return this;
    }

    public <NT, NO> PollSourceBuilder<I, NT, NO> withFormat(TransformerFormat<NT, NO> format) {
        return new PollSourceBuilder<I, NT, NO>(source, ports, eventMapping, schedule, errorRouter)
                .transformer(format.pipeline());
    }

    public PollSourceBuilder<I, T, O> eventMapping(EventMappingSpecification eventMapping) {
        this.eventMapping = eventMapping;
        return this;
    }

    public PollSourceBuilder<I, T, O> schedule(PollingConfiguration config) {
        this.schedule = config;
        return this;
    }

    public PollSourceBuilder<I, T, O> withErrorRouter(ErrorRouter errorRouter) {
        this.errorRouter = errorRouter;
        return this;
    }

    public Crawler create() {
        if (eventMapping.hasConflict())
            throw new IllegalArgumentException("Only one can be used at the same time. Please, use addDomainEvent(...) or addPartitionAttributes(...) but not both.");
        ItemCapturedHandler handler = new ItemCapturedHandler(
                source.sourceId(), ports,
                eventMapping.buildEmitter(ports.bus()),
                eventMapping.partitionAttributes(),
                errorRouter);
        PollingCrawlerStrategy<I, T, O> strategy = new PollingCrawlerStrategy<>(
                source, transformer, handler,
                schedule,
                errorRouter);
        return new Crawler(strategy, errorRouter);
    }
}
