package es.ulpgc.eii.spool;

/**
 * Classifies an {@link Event} according to its origin and semantic scope
 * within the spool-crawler framework.
 *
 * <p>This high-level classification allows consumers and routers to quickly
 * distinguish between events that carry business meaning and those that
 * are emitted by the infrastructure itself.</p>
 *
 * @see Event#eventCategory()
 * @since 1.0.0
 */
public enum EventCategory {

    /**
     * Indicates an event emitted by the platform or infrastructure layer.
     *
     * <p>Platform events typically reflect technical lifecycle facts such as
     * connector starts/stops, health checks, or ingestion errors — rather
     * than business-domain occurrences.</p>
     */
    PLATFORM,

    /**
     * Indicates an event that carries business-domain meaning.
     *
     * <p>Domain events represent things that happened in the problem domain,
     * such as a parking lot reaching full capacity or a sensor reporting
     * updated occupancy data. These are the primary events that downstream
     * consumers are expected to act upon.</p>
     */
    DOMAIN
}
