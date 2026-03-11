package software.spool.crawler.api.utils;

import software.spool.core.model.Event;
import software.spool.core.model.IdempotencyKey;
import software.spool.core.port.PayloadDeserializer;

import java.util.function.BiFunction;

public final class DomainEventMapping<D> {
    private final PayloadDeserializer<String, D> deserializer;
    private final BiFunction<D, IdempotencyKey, Event> toEvent;

    private DomainEventMapping(PayloadDeserializer<String, D> deserializer,
                               BiFunction<D, IdempotencyKey, Event> toEvent) {
        this.deserializer = deserializer;
        this.toEvent = toEvent;
    }

    public static <E extends Event> DomainEventMapping<E> of(
            PayloadDeserializer<String, E> deserializer) {
        return new DomainEventMapping<>(deserializer, (dto, key) -> dto);
    }

    public static <D> DomainEventMapping<D> of(
            PayloadDeserializer<String, D> deserializer,
            BiFunction<D, IdempotencyKey, Event> toEvent) {
        return new DomainEventMapping<>(deserializer, toEvent);
    }

    public Event resolve(String payload, IdempotencyKey key) {
        D dto = deserializer.deserialize(payload);
        return toEvent.apply(dto, key);
    }
}

