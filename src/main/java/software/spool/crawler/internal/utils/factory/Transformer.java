package software.spool.crawler.internal.utils.factory;

import software.spool.core.port.PayloadDeserializer;
import software.spool.core.port.RecordSerializer;
import software.spool.crawler.api.port.PayloadSplitter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Immutable record that composes the three processing stages of the crawler
 * pipeline into a single value object.
 *
 * <p>
 * The three stages are applied in order during each execution cycle:
 * <ol>
 * <li>{@link #deserializer()} – converts the raw source output into an
 * intermediate typed object.</li>
 * <li>{@link #splitter()} – expands the intermediate object into zero or more
 * individual records.</li>
 * <li>{@link #serializer()} – converts each record into a {@code String}
 * payload
 * ready for inbox storage.</li>
 * </ol>
 *
 * <p>
 * Pre-configured instances are available from {@link TransformerFactory}.
 * For custom assembly use one of the static factory methods:
 * </p>
 * 
 * <pre>{@code
 * Transformer<String, JsonNode, JsonNode> t = Transformer.of(myDeserializer, mySplitter, mySerializer);
 * }</pre>
 *
 * @param deserializer the deserialization stage
 * @param splitter     the splitting stage
 * @param serializer   the serialization stage
 * @param <T>          the intermediate parsed type
 * @param <O>          the individual record type
 */
public record Transformer<T, O>(
        PayloadDeserializer<T> deserializer,
        PayloadSplitter<T, O> splitter,
        RecordSerializer<O> serializer) {
    /**
     * Creates a new {@code Transformer} from the three given pipeline components.
     *
     * @param <P>          the intermediate parsed type
     * @param <T>          the individual record type
     * @param deserializer the deserializer stage; must not be {@code null}
     * @param splitter     the splitter stage; must not be {@code null}
     * @param serializer   the serializer stage; must not be {@code null}
     * @return a new {@code Transformer}
     */
    public static <P, T> Transformer<P, T> of(PayloadDeserializer<P> deserializer,
            PayloadSplitter<P, T> splitter, RecordSerializer<T> serializer) {
        return new Transformer<>(deserializer, splitter, serializer);
    }

    /**
     * Creates a {@code Transformer} that skips deserialization and splitting,
     * applying only the given serializer.
     *
     * <p>
     * The deserializer returns {@code null} for any input, and the splitter
     * wraps the raw payload in a single-element stream cast to {@code T}. Use
     * with caution — this is only appropriate when the source already produces
     * individual records of the correct output type.
     * </p>
     *
     * @param <T>        the record type
     * @param serializer the serializer to apply; must not be {@code null}
     * @return a new {@code Transformer} with no-op deserializer and splitter
     */
    public static <T> Transformer<Object, T> onlySerializer(RecordSerializer<T> serializer) {
        return new Transformer<>(r -> null, p -> Stream.of((T) p), serializer);
    }

    /**
     * Creates a {@code Transformer} that applies the deserializer and serializer
     * but uses a no-op splitter (each deserialized value becomes a single record).
     *
     * @param <P>          the intermediate and record type
     * @param deserializer the deserializer stage; must not be {@code null}
     * @param serializer   the serializer stage; must not be {@code null}
     * @return a new {@code Transformer} with an identity splitter
     */
    public static <P> Transformer<P, P> noSplitter(PayloadDeserializer<P> deserializer,
            RecordSerializer<P> serializer) {
        return new Transformer<>(deserializer, Stream::of, serializer);
    }

    /**
     * Applies the full pipeline (deserialize → split → serialize) to the given
     * input and returns a stream of serialized strings.
     *
     * @param <I>  the input type
     * @param poll the raw input from the source
     * @return a stream of serialized record strings
     */
    @SuppressWarnings("unchecked")
    public <I> Stream<String> transform(I poll) {
        if (Objects.nonNull(deserializer) && poll instanceof String)
            return splitter.split(deserializer.deserialize((String) poll)).map(serializer::serialize);
        return splitter.split((T) poll).map(serializer::serialize);
    }
}
