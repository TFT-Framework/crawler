package es.ulpgc.eii.spool.core.model;

import java.time.Instant;

public record EventReceived(
        String correlationId,
        String platformEventType,
        SourceType sourceType,
        Instant timestamp,
        int payloadSize
) implements PlatformEvent {
    @Override public String errorMessage() { return null; }

    public static EventReceived of(String correlationId, SourceType sourceType, int size) {
        return new EventReceived(correlationId, "RECEIVED", sourceType, Instant.now(), size);
    }
}
