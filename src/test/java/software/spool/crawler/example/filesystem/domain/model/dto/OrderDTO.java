package software.spool.crawler.example.filesystem.domain.model.dto;

import java.time.OffsetDateTime;

public record OrderDTO(
        String id,
        int seq,
        String userId,
        String status,
        String paymentMethod,
        String platform,
        String items,
        int itemCount,
        double subtotal,
        double tax,
        double shipping,
        double total,
        String currency,
        String shipStreet,
        String shipCity,
        String shipCountry,
        String shipZip,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
