package es.ulpgc.eii.spool.core.model;

import java.time.Instant;

public interface Event {
    String id();
    String correlationId();
    String idempotencyKey();
    EventCategory eventCategory();
    String eventType();
    Instant occurredAt();
    SchemaVersion schemaVersion();
}
