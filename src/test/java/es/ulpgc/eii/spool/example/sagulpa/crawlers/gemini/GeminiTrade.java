package es.ulpgc.eii.spool.example.sagulpa.crawlers.gemini;

import java.time.Instant;

public record GeminiTrade(
        long tid,
        String price,
        String amount,
        String type,
        Instant occurredAt
) {}