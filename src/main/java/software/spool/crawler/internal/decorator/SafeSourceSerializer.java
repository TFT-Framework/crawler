package software.spool.crawler.internal.decorator;

import software.spool.core.exception.SerializationException;
import software.spool.core.exception.SpoolException;
import software.spool.crawler.internal.port.SourceSerializer;

/**
 * Decorator for {@link SourceSerializer} that normalises unchecked exceptions
 * into typed {@link SerializationException} instances.
 *
 * <p>
 * If the delegate's {@link #serialize(Object, String)} method throws a
 * {@link SpoolException} subclass, it is re-thrown as-is. Any other
 * {@link Exception} is wrapped in a new {@link SerializationException}.
 * </p>
 *
 * @param <T> the individual record type to serialize
 */
public class SafeSourceSerializer<T> implements SourceSerializer<T> {
    private final SourceSerializer<T> serializer;

    private SafeSourceSerializer(SourceSerializer<T> serializer) {
        this.serializer = serializer;
    }

    /**
     * Creates a new {@code SafeSourceSerializer} wrapping the given delegate.
     *
     * @param <T>        the record type
     * @param serializer the serializer to wrap; must not be {@code null}
     * @return a new {@code SafeSourceSerializer} instance
     */
    public static <T> SafeSourceSerializer<T> of(SourceSerializer<T> serializer) {
        return new SafeSourceSerializer<>(serializer);
    }

    @Override
    public String serialize(T record, String sourceId) throws SpoolException {
        try {
            return serializer.serialize(record, sourceId);
        } catch (SpoolException e) {
            throw e;
        } catch (Exception e) {
            throw new SerializationException("Error while serializing: " + e.getMessage(), record.toString());
        }
    }
}
