package software.spool.crawler.internal.utils;

import software.spool.core.exception.DeserializationException;
import software.spool.core.exception.SerializationException;
import software.spool.core.model.Event;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.port.bus.BrokerMessage;
import software.spool.core.port.bus.Destination;
import software.spool.core.port.bus.EventPublisher;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DomainEventEmitter {
    private final EventPublisher bus;
    private final List<TypedDomainMapping> domainMappings;

    public DomainEventEmitter(EventPublisher bus, List<TypedDomainMapping> domainMappings) {
        this.bus = bus;
        this.domainMappings = domainMappings;
    }

    public Optional<TypedDomainMapping> emit(String payload, IdempotencyKey idempotencyKey) {
        if (domainMappings.isEmpty()) return Optional.empty();
        for (TypedDomainMapping typed : domainMappings) {
            try {
                Event event = typed.mapping().resolve(payload, idempotencyKey);
                bus.publish(new Destination("spool." + event.getClass().getSimpleName()), new BrokerMessage<>(event, event.getClass().getSimpleName(), Map.of()));
                return Optional.of(typed);
            } catch (DeserializationException | SerializationException ignored) {}
        }
        throw new DeserializationException(payload, "No matching domain event mapper found");
    }
}
