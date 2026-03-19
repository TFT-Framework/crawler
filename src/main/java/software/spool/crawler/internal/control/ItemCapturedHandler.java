package software.spool.crawler.internal.control;

import software.spool.core.control.Handler;
import software.spool.core.exception.DuplicateEventException;
import software.spool.core.model.*;
import software.spool.crawler.api.utils.CrawlerPorts;
import software.spool.crawler.internal.utils.DomainEventEmitter;
import software.spool.crawler.internal.utils.TypedDomainMapping;

import java.time.Instant;
import java.util.*;

public class ItemCapturedHandler implements Handler<String> {
    private final String sourceId;
    private final CrawlerPorts ports;
    private final DomainEventEmitter domainEventEmitter;
    private final List<String> defaultPartitionAttributes;

    public ItemCapturedHandler(String sourceId, CrawlerPorts ports, DomainEventEmitter domainEventEmitter,
                               List<String> defaultPartitionAttributes) {
        this.sourceId = sourceId;
        this.ports = ports;
        this.domainEventEmitter = domainEventEmitter;
        this.defaultPartitionAttributes = defaultPartitionAttributes;
    }

    @Override
    public void handle(String payload) {
        SourceItemCaptured captured = buildCapturedEvent(payload);
        Optional<TypedDomainMapping> matched = domainEventEmitter.emit(payload, captured.idempotencyKey());
        PartitionKeySchema schema = PartitionKeySchema.of(
                sourceId,
                matched.map(TypedDomainMapping::targetType).orElse(null),
                matched.map(TypedDomainMapping::partitionAttributes)
                        .orElse(defaultPartitionAttributes));
        try {
            ports.bus().emit(captured);
            IdempotencyKey receivedIdempotencyKey = ports.inboxWriter().receive(new InboxItem(
                    captured.idempotencyKey(), sourceId, schema,
                    payload, InboxItemStatus.UNPUBLISHED, Instant.now()
            ));
            if (Objects.isNull(receivedIdempotencyKey)) throw new DuplicateEventException(captured.idempotencyKey());
            ports.bus().emit(InboxItemStored.builder().from(captured).build());
        } catch (Exception e) {
            ports.errorRouter().dispatch(e);
        }
    }

    private SourceItemCaptured buildCapturedEvent(String payload) {
        return SourceItemCaptured.builder()
                .idempotencyKey(IdempotencyKey.of(sourceId, payload))
                .correlationId(UUID.randomUUID().toString())
                .build();
    }
}

