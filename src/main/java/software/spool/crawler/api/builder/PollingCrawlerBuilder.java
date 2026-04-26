package software.spool.crawler.api.builder;

import software.spool.core.adapter.jackson.RecordSerializerFactory;
import software.spool.core.adapter.otel.OpenTelemetryMetricsRegistry;
import software.spool.core.pipeline.ObservedStep;
import software.spool.core.pipeline.Pipeline;
import software.spool.core.pipeline.PipelineContext;
import software.spool.core.port.bus.Handler;
import software.spool.core.port.metrics.MetricsRegistry;
import software.spool.core.port.serde.EnrichmentRule;
import software.spool.core.port.serde.NamingConvention;
import software.spool.core.port.watchdog.ModuleHeartBeat;
import software.spool.core.utils.polling.PollingConfiguration;
import software.spool.core.utils.routing.ErrorRouter;
import software.spool.crawler.api.Crawler;
import software.spool.crawler.api.port.source.PollSource;
import software.spool.crawler.api.utils.CrawlerErrorRouter;
import software.spool.crawler.api.utils.CrawlerPorts;
import software.spool.crawler.api.utils.NormalizerFormat;
import software.spool.crawler.internal.control.ItemCapturedHandler;
import software.spool.crawler.internal.control.PayloadCapturedHandler;
import software.spool.crawler.internal.control.steps.*;
import software.spool.crawler.internal.port.decorator.SafePollSource;
import software.spool.crawler.internal.strategy.PollingCrawlerStrategy;
import software.spool.crawler.internal.utils.factory.Normalizer;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class PollingCrawlerBuilder<I> {
    private final PollSource<I> source;
    private final ModuleHeartBeat heartBeat;
    private CrawlerPorts ports;
    private List<EnrichmentRule> enrichRules;
    private String rootPath;
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

    public PollingCrawlerBuilder<I> enrichRules(List<EnrichmentRule> enrichRules) {
        this.enrichRules = enrichRules;
        return this;
    }

    public PollingCrawlerBuilder<I> rootPath(String rootPath) {
        this.rootPath = rootPath;
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

    public <P, E, R> Crawler createWith(Normalizer<P, E, R> normalizer) {
        validateRequiredFields();
        return new Crawler(initializeStrategy(normalizer, initHandler()), getErrorRouter(), heartBeat);
    }

    private <P, E, R> PollingCrawlerStrategy<I, P, E, R> initializeStrategy(Normalizer<P, E, R> normalizer, Handler<String> handler) {
        return new PollingCrawlerStrategy<>(source, normalizer, handler, schedule, getErrorRouter());
    }

    private ErrorRouter getErrorRouter() {
        return Objects.requireNonNullElse(errorRouter, CrawlerErrorRouter.defaults(ports.bus()));
    }

    private PayloadCapturedHandler initHandler() {
        return new PayloadCapturedHandler(initializePipeline(), source.sourceId(), getErrorRouter());
    }

    private Pipeline<PipelineContext, PipelineContext> initializePipeline() {
        return Pipeline.<PipelineContext>start()
                .add(new ObservedStep<>("measure-size", new PayloadSizeMetricStep(buildHistogram())))
                .add(new ObservedStep<>("build-captured", new BuildCapturedEventStep()))
                .add(new ObservedStep<>("emit-domain-event",
                        new PublishDomainEventStep(eventMapping.buildEmitter(ports.bus()))))
                .add(new ObservedStep<>("publish-captured", new PublishCapturedEvent(ports.bus())))
                .add(new ObservedStep<>("store-envelope",
                        new BuildAndStoreEnvelopeStep(ports.inboxWriter(),
                                RecordSerializerFactory.record(),
                                eventMapping.partitionAttributes())))
                .add(new ObservedStep<>("publish-stored", new PublishEnvelopeStoredStep(ports.bus())));
    }

    private MetricsRegistry.LongHistogramMetric buildHistogram() {
        return new OpenTelemetryMetricsRegistry()
                .histogram("spool.captured.payload.size", "", "By");
    }

    private ItemCapturedHandler initializeHandler() {
        return new ItemCapturedHandler(
                source.sourceId(), ports,
                eventMapping.buildEmitter(ports.bus()),
                eventMapping.partitionAttributes(),
                getErrorRouter());
    }

    public <P, E, R> Crawler createWith(NormalizerFormat<P, E, R> format) {
        return createWith(format.pipelineWith(enrichRules, rootPath));
    }

    private void validateRequiredFields() {
        Objects.requireNonNull(ports, "CrawlerPorts must be set before calling create()");
    }
}
