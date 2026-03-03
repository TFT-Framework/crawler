package software.spool.crawler.internal.port;

import software.spool.core.exception.SpoolException;

/**
 * Internal SPI for converting an individual record into a serialized
 * {@code String} payload ready to be stored in the inbox.
 *
 * <p>
 * This is the third stage of the three-stage processing pipeline:
 * deserialize → split → <b>serialize</b>.
 * </p>
 *
 * <p>
 * Pre-built implementations are available via
 * {@link software.spool.crawler.internal.utils.factory.SerializerFactory}.
 * </p>
 *
 * @param <T> the individual record type to serialize
 */
public interface SourceSerializer<T> {
    /**
     * Converts the given record into a {@code String} payload.
     *
     * @param record   the record to serialize; must not be {@code null}
     * @param sourceId the identifier of the source that produced the record;
     *                 may be used to enrich the serialized output
     * @return the serialized string representation; must not be {@code null}
     * @throws SpoolException if the record cannot be serialized
     */
    String serialize(T record, String sourceId) throws SpoolException;
}
