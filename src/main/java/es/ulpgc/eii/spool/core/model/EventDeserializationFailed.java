package es.ulpgc.eii.spool.core.model;

import java.time.Instant;

public record EventDeserializationFailed(
        String correlationId,
        String platformEventType,
        SourceType sourceType,
        Instant timestamp,
        String errorMessage,
        String rawPayload
) implements PlatformEvent {
    public static EventDeserializationFailed of(String raw, String error, SourceType sourceType) {
        return new EventDeserializationFailed("", "DESERIALIZATION_FAILED", sourceType, Instant.now(), error, raw);
    }
}
