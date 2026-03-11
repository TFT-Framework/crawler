package software.spool.crawler.example.filesystem.domain.model;

import software.spool.core.model.Event;
import software.spool.crawler.example.filesystem.domain.model.dto.OrderDTO;

import java.time.Instant;

public record OrderReceived(
        String orderId,
        String userId,
        String status,
        String paymentMethod,
        String platform,
        double total,
        String currency,
        Instant occurredAt
) implements Event {

    @Override
    public String eventId() { return orderId; }

    @Override
    public String causationId() { return orderId; }

    @Override
    public String correlationId() { return orderId; }

    @Override
    public Instant timestamp() { return occurredAt; }

    public static OrderReceived from(OrderDTO dto) {
        return new OrderReceived(
                dto.id(),
                dto.userId(),
                dto.status(),
                dto.paymentMethod(),
                dto.platform(),
                dto.total(),
                dto.currency(),
                dto.createdAt().toInstant()
        );
    }
}
