package software.spool.crawler.api.utils;

import software.spool.core.port.serde.EnrichmentRule;
import software.spool.crawler.internal.utils.factory.Normalizer;

import java.util.List;

/**
 * Describes a named data-processing pipeline that converts raw source data into
 * serialized string records suitable for inbox storage.
 *
 * <p>
 * A {@code ProcessorFormat} combines three processing stages:
 * <ol>
 * <li><b>Deserialization</b> – converts the raw type {@code R} into an
 * intermediate parsed type {@code P}.</li>
 * <li><b>Splitting</b> – expands the parsed value {@code P} into zero or more
 * typed records of type {@code P} (e.g. individual elements of a JSON
 * array).</li>
 * <li><b>Serialization</b> – converts each record {@code P} into a
 * {@code String} payload ready to be stored in the inbox.</li>
 * </ol>
 *
 * <p>
 * Pre-built formats are available in
 * {@link StandardNormalizer}.
 * Custom formats can be created by implementing this interface:
 * </p>
 * 
 * <pre>{@code
 * ProcessorFormat<String, MyType, MyRecord> MY_FORMAT = () -> Normalizer.of(MyDeserializer::new, MySplitter::new,
 *         MySerializer::new);
 * }</pre>
 *
 * @param <P> the intermediate parsed type after deserialization
 * @param <R> the individual record type produced by the splitter
 */
@FunctionalInterface
public interface NormalizerFormat<P, E, R> {

    /**
     * Returns a fully configured {@link Normalizer} that can be applied to raw
     * data read from a source.
     *
     * @return a {@link Normalizer} wiring together the deserializer, splitter, and
     *         serializer for this format
     */
    Normalizer<P, E, R> pipelineWith(List<EnrichmentRule> enrichmentRules, String rootPath);
}
