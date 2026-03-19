package software.spool.crawler.internal.utils;

import software.spool.core.exception.DeserializationException;
import software.spool.core.exception.SerializationException;
import software.spool.core.model.IdempotencyKey;
import software.spool.core.port.EventBusEmitter;

import java.util.List;
import java.util.Optional;

public class DomainEventEmitter {
    private final EventBusEmitter bus;
    private final List<TypedDomainMapping> domainMappings;

    public DomainEventEmitter(EventBusEmitter bus, List<TypedDomainMapping> domainMappings) {
        this.bus = bus;
        this.domainMappings = domainMappings;
    }

    public Optional<TypedDomainMapping> emit(String payload, IdempotencyKey idempotencyKey) {
        if (domainMappings.isEmpty()) return Optional.empty();
        for (TypedDomainMapping typed : domainMappings) {
            try {
                bus.emit(typed.mapping().resolve(payload, idempotencyKey));
                return Optional.of(typed);
            } catch (DeserializationException | SerializationException ignored) {}
        }
        throw new DeserializationException(payload, "No matching domain event mapper found");
    }
}
