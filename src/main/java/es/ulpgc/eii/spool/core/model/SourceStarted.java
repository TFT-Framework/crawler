package es.ulpgc.eii.spool.core.model;

import java.time.Instant;

public record SourceStarted(
        String correlationId,
        String platformEventType,
        SourceType sourceType,
        Instant timestamp
) implements PlatformEvent {
    @Override public String errorMessage() {return null;}

    public static SourceStarted of(SourceType sourceType) {
        return new SourceStarted(null, "SOURCE_STARTED", sourceType, Instant.now());
    }
}
