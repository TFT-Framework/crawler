package software.spool.model;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record RawDataWrittenToInbox(
        String eventId,
        Instant timestamp,
        String eventType,
        String source,
        String errorMessage,
        String idempotencyKey,
        Optional<String> payload
) implements SpoolEvent {

    public static DataWrittenToInboxBuilder from(String source) {
        return new DataWrittenToInboxBuilder(source);
    }

    public static class DataWrittenToInboxBuilder {

        private final String source;
        private String errorMessage;
        private String payload;
        private String idempotencyKey;

        public DataWrittenToInboxBuilder(String source) {
            this.source = source;
        }

        public DataWrittenToInboxBuilder withErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public DataWrittenToInboxBuilder withPayload(String payload) {
            this.payload = payload;
            return this;
        }

        public DataWrittenToInboxBuilder withIdempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public RawDataWrittenToInbox create() {
            return new RawDataWrittenToInbox(
                    UUID.randomUUID().toString(),          // eventId
                    Instant.now(),                         // timestamp
                    "DataWrittenToInbox",                  // eventType
                    source,
                    errorMessage,
                    idempotencyKey,
                    Optional.ofNullable(payload)           // payload como Optional
            );
        }
    }
}
