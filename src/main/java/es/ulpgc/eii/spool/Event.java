package es.ulpgc.eii.spool;

import java.time.Instant;

/**
 * Base contract for all events handled by the spool-crawler framework.
 *
 * <p>Every domain or platform event must implement this interface to ensure
 * a consistent set of metadata is always available for tracing, idempotency
 * control, routing, and schema evolution.</p>
 *
 * <p>Implementations are encouraged to be immutable (e.g. Java records).</p>
 *
 * @see EventCategory
 * @see SchemaVersion
 * @since 1.0.0
 */
public interface Event {

    /**
     * Returns the unique identifier of this event instance.
     *
     * <p>Must be globally unique across all event types and sources.
     * UUIDs (v4 or v7) are recommended.</p>
     *
     * @return a non-null, non-empty string identifying this event
     */
    String id();

    /**
     * Returns the correlation ID linking this event to a broader
     * business transaction or request chain.
     *
     * <p>Useful for distributed tracing: all events triggered by the
     * same originating action should share the same correlation ID.</p>
     *
     * @return a non-null correlation identifier
     */
    String correlationId();

    /**
     * Returns the idempotency key for this event.
     *
     * <p>Consumers can use this key to detect and safely discard
     * duplicate deliveries without reprocessing side effects.</p>
     *
     * @return a non-null idempotency key
     */
    String idempotencyKey();

    /**
     * Returns the category that classifies this event at a high level.
     *
     * @return {@link EventCategory#DOMAIN} for business events,
     *         {@link EventCategory#PLATFORM} for infrastructure/system events
     */
    EventCategory eventCategory();

    /**
     * Returns the specific type name of this event.
     *
     * <p>Follows a dot-separated naming convention, e.g.
     * {@code "parking.occupancy.updated"} or {@code "user.account.created"}.</p>
     *
     * @return a non-null, non-empty event type identifier
     */
    String eventType();

    /**
     * Returns the instant at which this event occurred in the source system.
     *
     * <p>This timestamp reflects the business occurrence time, not the
     * ingestion or processing time.</p>
     *
     * @return a non-null {@link Instant} representing when the event happened
     */
    Instant occurredAt();

    /**
     * Returns the schema version of the event payload.
     *
     * <p>Used to support schema evolution and backward-compatibility checks.
     * Must follow semantic versioning (e.g. {@code "1.0.0"}).</p>
     *
     * @return a non-null {@link SchemaVersion} describing the payload schema
     */
    SchemaVersion schemaVersion();
}
