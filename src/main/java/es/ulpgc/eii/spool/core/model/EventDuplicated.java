package es.ulpgc.eii.spool.core.model;

import java.time.Instant;

public record EventDuplicated(
        String correlationId,
        String platformEventType,
        SourceType sourceType,
        Instant timestamp,
        String errorMessage
) implements PlatformEvent {
    public static EventDuplicated of(String correlationId, SourceType sourceType) {
        return new EventDuplicated(correlationId, "DUPLICATED", sourceType, Instant.now(), null);
    }
}
