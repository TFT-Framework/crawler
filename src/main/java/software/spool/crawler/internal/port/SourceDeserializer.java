package software.spool.crawler.internal.port;

import software.spool.core.exception.DeserializationException;

/**
 * Internal SPI for converting a raw source value into a typed intermediate
 * object.
 *
 * <p>
 * This is the first stage of the three-stage processing pipeline:
 * <b>deserialize</b> → split → serialize.
 * </p>
 *
 * <p>
 * Pre-built implementations are available via
 * {@link software.spool.crawler.internal.utils.factory.DeserializerFactory}.
 * </p>
 *
 * @param <R> the raw type produced by the
 *            {@link software.spool.crawler.api.source.PollSource}
 * @param <T> the typed intermediate representation
 */
public interface SourceDeserializer<R, T> {
    /**
     * Converts the given raw value into a typed intermediate object.
     *
     * @param source the raw value to deserialize; may be {@code null} if the
     *               source legitimately produces empty responses
     * @return the deserialized representation; must not be {@code null}
     * @throws DeserializationException if the value cannot be parsed or converted
     */
    T deserialize(R source) throws DeserializationException;
}
