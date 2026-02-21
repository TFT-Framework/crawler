package es.ulpgc.eii.spool.core.model;

import java.time.Instant;

public record SourceStopped(
        String correlationId,
        String platformEventType,
        SourceType sourceType,
        Instant timestamp,
        String errorMessage
) implements PlatformEvent {

    public static SourceStopped of(SourceType sourceType, String errorMessage) {
        return new SourceStopped(null, "SOURCE_STOPPED", sourceType, Instant.now(), errorMessage);
    }
}
