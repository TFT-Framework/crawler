package es.ulpgc.eii.spool.core.model;

import java.time.Instant;

public record RawInboxEvent(
    String eventId,
    String sourceId,
    Instant occurredAt,
    String payload,
    String contentType
) {}
