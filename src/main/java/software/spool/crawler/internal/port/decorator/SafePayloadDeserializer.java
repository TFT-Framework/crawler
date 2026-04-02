package software.spool.crawler.internal.port.decorator;

import software.spool.core.exception.DeserializationException;
import software.spool.core.exception.SpoolException;
import software.spool.core.port.serde.PayloadDeserializer;

/**
 * Decorator for {@link PayloadDeserializer} that normalises unchecked exceptions
 * into typed {@link DeserializationException} instances.
 *
 * <p>
 * If the delegate's {@link #deserialize(String)} method throws a
 * {@link SpoolException} subclass, it is re-thrown as-is. Any other
 * {@link Exception} is wrapped in a new {@link DeserializationException}.
 * </p>
 *
 * @param <T> the intermediate type produced after deserialization
 */
public class SafePayloadDeserializer<T> implements PayloadDeserializer<T> {
    private final PayloadDeserializer<T> deserializer;

    private SafePayloadDeserializer(PayloadDeserializer<T> deserializer) {
        this.deserializer = deserializer;
    }

    /**
     * Creates a new {@code SafePayloadDeserializer} wrapping the given delegate.
     *
     * @param <T>          the intermediate output type
     * @param deserializer the deserializer to wrap; must not be {@code null}
     * @return a new {@code SafePayloadDeserializer} instance
     */
    public static <T> SafePayloadDeserializer<T> of(PayloadDeserializer<T> deserializer) {
        return new SafePayloadDeserializer<>(deserializer);
    }

    @Override
    public T deserialize(String payload) throws DeserializationException {
        try {
            return deserializer.deserialize(payload);
        } catch (SpoolException e) {
            throw e;
        } catch (Exception e) {
            throw new DeserializationException(payload, e.getMessage());
        }
    }
}
