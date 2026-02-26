package es.ulpgc.eii.spool.core.model;

import java.time.Instant;
import java.util.UUID;

public record DataWrittenToInbox(
        Instant timestamp,
        String source,
        String errorMessage,
        String payload,
        UUID idempotencyKey

) implements FrameworkEvent {
    public static DataWrittenToInboxBuilder from(String source) {
        return new DataWrittenToInboxBuilder(source);
    }

    protected static class DataWrittenToInboxBuilder {
        private final String source;
        private String errorMessage;
        private String payload;
        private UUID idempotencyKey;

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

        public DataWrittenToInboxBuilder withIdempotencyKey(UUID idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public DataWrittenToInbox create() {
            return new DataWrittenToInbox(Instant.now(), source, errorMessage, payload, idempotencyKey);
        }
    }
}
