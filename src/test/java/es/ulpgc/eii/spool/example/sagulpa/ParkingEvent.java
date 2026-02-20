package es.ulpgc.eii.spool.example.sagulpa;

import es.ulpgc.eii.spool.domain.Event;
import es.ulpgc.eii.spool.domain.EventCategory;
import es.ulpgc.eii.spool.domain.SchemaVersion;

import java.time.Instant;

public record ParkingEvent(
        String id,
        String correlationId,
        String idempotencyKey,
        EventCategory eventCategory,
        String eventType,
        Instant occurredAt,
        SchemaVersion schemaVersion,
        String parkingLotId,
        int freeSpots
) implements Event {}
