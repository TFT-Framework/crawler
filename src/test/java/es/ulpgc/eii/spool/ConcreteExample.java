package es.ulpgc.eii.spool;

import es.ulpgc.eii.spool.core.model.Event;
import es.ulpgc.eii.spool.core.model.EventCategory;
import es.ulpgc.eii.spool.core.model.SchemaVersion;

import java.time.Instant;

public record ConcreteExample(String id) implements Event {
    @Override
    public String correlationId() {
        return "";
    }

    @Override
    public String idempotencyKey() {
        return "";
    }

    @Override
    public EventCategory eventCategory() {
        return null;
    }

    @Override
    public String eventType() {
        return "";
    }

    @Override
    public Instant occurredAt() {
        return null;
    }

    @Override
    public SchemaVersion schemaVersion() {
        return null;
    }
}
