package software.spool.crawler.api.builder;

import software.spool.core.model.Event;
import software.spool.core.model.IdempotencyKey;
import software.spool.core.port.EventBus;
import software.spool.core.port.EventBusEmitter;
import software.spool.core.port.PayloadDeserializer;
import software.spool.core.utils.DomainEventMapping;
import software.spool.core.utils.NamingConvention;
import software.spool.crawler.internal.utils.DomainEventEmitter;
import software.spool.crawler.internal.utils.TypedDomainMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class EventMappingSpecification {
    private final NamingConvention namingConvention;
    private final List<TypedDomainMapping> domainMappings;
    private final List<String> defaultPartitionAttributes;

    public EventMappingSpecification(NamingConvention namingConvention) {
        this.namingConvention = namingConvention;
        this.domainMappings = new ArrayList<>();
        this.defaultPartitionAttributes = new ArrayList<>();
    }

    private <D> PayloadDeserializer<D> deserializerFor(Class<D> type) {
        return namingConvention.deserializerFor(type);
    }

    public EventMappingSpecification addDomainEvent(Class<? extends Event> eventType, String... partitionAttributes) {
        domainMappings.add(new TypedDomainMapping(eventType,
                DomainEventMapping.of(deserializerFor(eventType)),
                List.of(partitionAttributes)));
        return this;
    }

    public <D> EventMappingSpecification addDomainEvent(Class<D> dtoType, BiFunction<D, IdempotencyKey, Event> toEvent, String... partitionAttributes) {
        domainMappings.add(new TypedDomainMapping(dtoType,
                DomainEventMapping.of(deserializerFor(dtoType), toEvent),
                List.of(partitionAttributes)));
        return this;
    }

    public EventMappingSpecification addPartitionAttributes(String... attributes) {
        defaultPartitionAttributes.addAll(List.of(attributes));
        return this;
    }

    public boolean hasConflict() {
        return !domainMappings.isEmpty() && !defaultPartitionAttributes.isEmpty();
    }

    public DomainEventEmitter buildEmitter(EventBusEmitter bus) {
        return new DomainEventEmitter(bus, domainMappings);
    }

    public List<String> partitionAttributes() {
        return defaultPartitionAttributes;
    }
}
