package software.spool.model;

import java.time.Instant;
import java.util.Optional;

public record SourceFailed(
        String eventId,
        String eventType,
        Instant timestamp,
        Optional<String> payload
) implements SpoolEvent {
}
