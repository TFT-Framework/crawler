package software.spool.crawler.internal.port.decorator;

import software.spool.core.exception.SerializationException;
import software.spool.core.exception.SpoolException;
import software.spool.core.port.serde.RecordSerializer;

/**
 * Decorator for {@link RecordSerializer} that normalises unchecked exceptions
 * into typed {@link SerializationException} instances.
 *
 * <p>
 * If the delegate's {@link #serialize(Object)} method throws a
 * {@link SpoolException} subclass, it is re-thrown as-is. Any other
 * {@link Exception} is wrapped in a new {@link SerializationException}.
 * </p>
 *
 * @param <T> the individual record type to serialize
 */
public class SafeRecordSerializer<T> implements RecordSerializer<T> {
    private final RecordSerializer<T> serializer;

    private SafeRecordSerializer(RecordSerializer<T> serializer) {
        this.serializer = serializer;
    }

    /**
     * Creates a new {@code SafeRecordSerializer} wrapping the given delegate.
     *
     * @param <T>        the record type
     * @param serializer the serializer to wrap; must not be {@code null}
     * @return a new {@code SafeRecordSerializer} instance
     */
    public static <T> SafeRecordSerializer<T> of(RecordSerializer<T> serializer) {
        return new SafeRecordSerializer<>(serializer);
    }

    @Override
    public String serialize(T record) throws SpoolException {
        try {
            return serializer.serialize(record);
        } catch (SpoolException e) {
            throw e;
        } catch (Exception e) {
            throw new SerializationException(record.toString(), e.getMessage());
        }
    }
}
