package software.spool.crawler.internal.decorator;

import software.spool.core.exception.DeserializationException;
import software.spool.core.exception.SpoolException;
import software.spool.crawler.internal.port.SourceDeserializer;

/**
 * Decorator for {@link SourceDeserializer} that normalises unchecked exceptions
 * into typed {@link DeserializationException} instances.
 *
 * <p>
 * If the delegate's {@link #deserialize(Object)} method throws a
 * {@link SpoolException} subclass, it is re-thrown as-is. Any other
 * {@link Exception} is wrapped in a new {@link DeserializationException}.
 * </p>
 *
 * @param <R> the raw type to deserialize from
 * @param <T> the intermediate type produced after deserialization
 */
public class SafeSourceDeserializer<R, T> implements SourceDeserializer<R, T> {
    private final SourceDeserializer<R, T> deserializer;

    private SafeSourceDeserializer(SourceDeserializer<R, T> deserializer) {
        this.deserializer = deserializer;
    }

    /**
     * Creates a new {@code SafeSourceDeserializer} wrapping the given delegate.
     *
     * @param <R>          the raw input type
     * @param <T>          the intermediate output type
     * @param deserializer the deserializer to wrap; must not be {@code null}
     * @return a new {@code SafeSourceDeserializer} instance
     */
    public static <R, T> SafeSourceDeserializer<R, T> of(SourceDeserializer<R, T> deserializer) {
        return new SafeSourceDeserializer<>(deserializer);
    }

    @Override
    public T deserialize(R source) throws DeserializationException {
        try {
            return deserializer.deserialize(source);
        } catch (SpoolException e) {
            throw e;
        } catch (Exception e) {
            throw new DeserializationException("Error when deserializing: " + e.getMessage(), e);
        }
    }
}
