package es.ulpgc.eii.spool.core.model;

import java.time.Instant;

public interface PlatformEvent {
    String correlationId();
    String platformEventType();
    SourceType sourceType();
    Instant timestamp();
    String errorMessage();
}
