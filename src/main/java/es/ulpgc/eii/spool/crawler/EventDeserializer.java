package es.ulpgc.eii.spool.crawler;

import es.ulpgc.eii.spool.Event;

/**
 * Strategy for converting a raw payload of type {@code R} into a typed
 * {@link Event} of type {@code T}.
 *
 * <p>{@code EventDeserializer} is the bridge between what a source delivers
 * (e.g. a raw JSON string, a JDBC row, a binary Avro record) and the
 * strongly-typed domain event that the rest of the system works with.
 * Keeping this concern separate makes both the source and the event model
 * independently testable and replaceable.</p>
 *
 * <p>Example — deserializing a JSON string into a domain event:</p>
 * <pre>{@code
 * EventDeserializer<String, ParkingEvent> deserializer =
 *         raw -> JsonMapper.parse(raw, ParkingEvent.class);
 * }</pre>
 *
 * <p>As a functional interface, it can be implemented as a lambda or
 * method reference wherever a deserializer is expected.</p>
 *
 * @param <R> the type of the raw input payload
 *            (e.g. {@code String}, {@code byte[]}, {@code JsonNode})
 * @param <T> the target {@link Event} type produced after deserialization
 * @see CrawlerSource
 * @see es.ulpgc.eii.spool.crawler.strategy.PullCrawlerStrategy
 * @since 1.0.0
 */
@FunctionalInterface
public interface EventDeserializer<R, T extends Event> {

    /**
     * Deserializes a raw payload into a typed {@link Event}.
     *
     * <p>Implementations should be stateless and side-effect free.
     * If the payload is malformed or cannot be mapped, throwing an
     * unchecked exception (e.g. {@link IllegalArgumentException}) is
     * recommended so that the calling strategy can handle it via its
     * {@code onError} hook.</p>
     *
     * @param raw the raw payload to deserialize; must not be {@code null}
     * @return a non-null, fully populated instance of {@code T}
     * @throws IllegalArgumentException if {@code raw} cannot be deserialized
     *                                  into a valid event
     */
    T deserialize(R raw);
}
