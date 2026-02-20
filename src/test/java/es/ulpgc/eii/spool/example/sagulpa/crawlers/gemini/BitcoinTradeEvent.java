package es.ulpgc.eii.spool.example.sagulpa.crawlers.gemini;

import es.ulpgc.eii.spool.domain.Event;
import es.ulpgc.eii.spool.domain.EventCategory;
import es.ulpgc.eii.spool.domain.SchemaVersion;
import java.time.Instant;

public record BitcoinTradeEvent(
        String id,
        String correlationId,
        String idempotencyKey,
        EventCategory eventCategory,
        String eventType,
        Instant occurredAt,
        SchemaVersion schemaVersion,
        String price,
        String amount,
        String tradeType
) implements Event {}