package software.spool.crawler.internal.control;

import software.spool.core.adapter.jackson.RecordSerializerFactory;
import software.spool.core.adapter.otel.OpenTelemetryMetricsRegistry;
import software.spool.core.exception.DuplicateEventException;
import software.spool.core.model.*;
import software.spool.core.model.event.EnvelopeStored;
import software.spool.core.model.event.SourcePayloadCaptured;
import software.spool.core.model.vo.*;
import software.spool.core.port.bus.BrokerMessage;
import software.spool.core.port.bus.Destination;
import software.spool.core.port.bus.Handler;
import software.spool.core.port.metrics.MetricsRegistry;
import software.spool.core.utils.routing.ErrorRouter;
import software.spool.crawler.api.utils.CrawlerPorts;
import software.spool.crawler.internal.utils.DomainEventEmitter;
import software.spool.crawler.internal.utils.TypedDomainMapping;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

public class ItemCapturedHandler implements Handler<String> {
    private final String sourceId;
    private final CrawlerPorts ports;
    private final DomainEventEmitter domainEventEmitter;
    private final List<String> defaultPartitionAttributes;
    private final ErrorRouter errorRouter;

    public ItemCapturedHandler(String sourceId, CrawlerPorts ports, DomainEventEmitter domainEventEmitter,
                               List<String> defaultPartitionAttributes, ErrorRouter errorRouter) {
        this.sourceId = sourceId;
        this.ports = ports;
        this.errorRouter = errorRouter;
        this.domainEventEmitter = domainEventEmitter;
        this.defaultPartitionAttributes = defaultPartitionAttributes;
    }

    @Override
    public void handle(String payload) {
        OpenTelemetryMetricsRegistry registry = new OpenTelemetryMetricsRegistry();
        MetricsRegistry.LongHistogramMetric his = registry.histogram("spool.captured.payload.size", "", "By");
        his.record(payload.getBytes(StandardCharsets.UTF_8).length, Map.of());
        SourcePayloadCaptured captured = buildCapturedEvent(payload);
        Optional<TypedDomainMapping> matched = domainEventEmitter.emit(payload, captured.idempotencyKey());
        PartitionKeySchema schema = PartitionKeySchema.of(
                sourceId,
                matched.map(TypedDomainMapping::targetType).orElse(null),
                matched.map(TypedDomainMapping::partitionAttributes)
                        .orElse(defaultPartitionAttributes));
        try {
            ports.bus().publish(new Destination("spool." + captured.getClass().getSimpleName()), new BrokerMessage<>(captured, captured.getClass().getSimpleName(), Map.of()));
            EventMetadata metadata = new EventMetadata()
                    .set(EventMetadataKey.SOURCE, sourceId)
                    .set(EventMetadataKey.PARTITION_SCHEMA, RecordSerializerFactory.record().serialize(schema));
            matched.map(TypedDomainMapping::targetType).ifPresent(type -> metadata.set(EventMetadataKey.TYPE, type.toString()));
            IdempotencyKey receivedIdempotencyKey = ports.inboxWriter().receive(new Envelope(
                    captured.idempotencyKey(), metadata,
                    payload, EnvelopeStatus.CAPTURED, 0, Instant.now()
            ));
            if (Objects.isNull(receivedIdempotencyKey)) throw new DuplicateEventException(captured.idempotencyKey());
            EnvelopeStored storedEvent = EnvelopeStored.builder().from(captured).build();
            ports.bus().publish(new Destination("spool." + storedEvent.getClass().getSimpleName()), new BrokerMessage<>(storedEvent, storedEvent.getClass().getSimpleName(), Map.of()));
        } catch (Exception e) {
            errorRouter.dispatch(e);
        }
    }

    private SourcePayloadCaptured buildCapturedEvent(String payload) {
        return SourcePayloadCaptured.builder()
                .idempotencyKey(IdempotencyKey.of(sourceId, payload))
                .correlationId(UUID.randomUUID().toString())
                .build();
    }
}

