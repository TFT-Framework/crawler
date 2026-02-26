package es.ulpgc.eii.spool.core.model;

import java.time.Instant;

public interface FrameworkEvent {
    Instant timestamp();
    String source();
    String errorMessage();
}
