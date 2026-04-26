package software.spool.crawler.internal.utils.factory;

import software.spool.core.port.serde.*;
import software.spool.crawler.api.port.PayloadSplitter;

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
 * Pre-configured instances are available from {@link NormalizerFactory}.
 * For custom assembly use one of the static factory methods:
 * </p>
 * 
 * <pre>{@code
 * Normalizer<String, JsonNode, JsonNode> t = Normalizer.of(myDeserializer, mySplitter, mySerializer);
 * }</pre>
 *
 * @param deserializer the deserialization stage
 * @param splitter     the splitting stage
 * @param serializer   the serialization stage
 */
public record Normalizer<P, E, R>(
        PayloadDeserializer<P> deserializer,
        PayloadExtractor<P, E> extractor,
        PayloadLocator<P> locator,
        PayloadSplitter<P, R> splitter,
        RecordEnricher<R, E> enricher,
        RecordSerializer<R> serializer) {
    /**
     * Creates a new {@code Normalizer} from the three given pipeline components.
     *
     * @param <P>          the intermediate parsed type
     * @param <R>          the individual record type
     * @param deserializer the deserializer stage; must not be {@code null}
     * @param splitter     the splitter stage; must not be {@code null}
     * @param serializer   the serializer stage; must not be {@code null}
     * @return a new {@code Normalizer}
     */
    public static <P, E, R> Normalizer<P, E, R> of(PayloadDeserializer<P> deserializer, PayloadExtractor<P, E> extractor, PayloadLocator<P> locator,
                                             PayloadSplitter<P, R> splitter, RecordEnricher<R, E> enricher, RecordSerializer<R> serializer) {
        return new Normalizer<>(deserializer, extractor, locator, splitter, enricher, serializer);
    }

    @SuppressWarnings("unchecked")
    public <I> Stream<String> transform(I poll) {
        P parsed = poll instanceof String raw ? deserializer.deserialize(raw) : (P) poll;
        return enricher.enrich(splitter.split(locator.locate(parsed)), extractor.extract(parsed))
                .map(serializer::serialize);
    }
}
