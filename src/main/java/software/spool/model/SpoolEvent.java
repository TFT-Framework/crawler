package software.spool.model;

import java.time.Instant;
import java.util.Optional;

public interface SpoolEvent {
    String eventId();
    String eventType();
    Instant timestamp();
    default Optional<String> payload() {
        return Optional.empty();
    }
}
