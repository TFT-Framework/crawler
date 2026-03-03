package software.spool.crawler.internal.utils.factory;

import com.fasterxml.jackson.databind.JsonNode;
import software.spool.crawler.internal.port.SourceDeserializer;
import software.spool.crawler.internal.port.SourceSplitter;
import software.spool.crawler.internal.port.SourceSerializer;

import java.sql.ResultSet;
import java.util.Map;

/**
 * Factory class that assembles pre-configured {@link Transformer} instances for
 * common data formats.
 *
 * <p>
 * The factory methods are referenced directly from the constants in
 * {@link software.spool.crawler.api.Formats} as method references:
 * 
 * <pre>{@code
 * ProcessorFormat<String, JsonNode, JsonNode> JSON_ARRAY = TransformerFactory::jsonArray;
 * }</pre>
 *
 * <p>
 * Custom transformers can be created with the generic
 * {@link #of(SourceDeserializer, SourceSplitter, SourceSerializer)} variant.
 * </p>
 */
public class TransformerFactory {
    /**
     * Creates a transformer that parses a JSON string into individual
     * {@link JsonNode} array elements.
     *
     * @return a {@link Transformer} for JSON array strings
     */
    public static Transformer<String, JsonNode, JsonNode> jsonArray() {
        return new Transformer<>(
                DeserializerFactory.json(),
                SplitterFactory.jsonArray(),
                SerializerFactory.jsonNode());
    }

    /**
     * Creates a transformer that parses a YAML string into individual
     * {@link JsonNode} sequence elements.
     *
     * @return a {@link Transformer} for YAML array strings
     */
    public static Transformer<String, JsonNode, JsonNode> yamlArray() {
        return new Transformer<>(
                DeserializerFactory.yamlArray(),
                SplitterFactory.jsonArray(),
                SerializerFactory.jsonNode());
    }

    /**
     * Creates a transformer for JDBC {@link ResultSet} sources.
     *
     * <p>
     * The deserialization step is a no-op (the {@code ResultSet} is passed
     * through unchanged). Each row is then projected to a
     * {@code Map<String, Object>} and serialized to JSON.
     * </p>
     *
     * @return a {@link Transformer} for {@code ResultSet} sources
     */
    public static Transformer<ResultSet, ResultSet, Map<String, Object>> resultSet() {
        return new Transformer<>(
                r -> r, // No-op para ResultSet
                SplitterFactory.resultSet(),
                SerializerFactory.map());
    }

    /**
     * Creates a transformer from the three individual pipeline components.
     *
     * <p>
     * Use this overload to build custom transformers that are not covered by
     * the pre-built ones.
     * </p>
     *
     * @param <R>          the raw source type
     * @param <P>          the intermediate parsed type
     * @param <T>          the individual record type
     * @param deserializer the deserialization stage
     * @param splitter     the splitting stage
     * @param serializer   the serialization stage
     * @return a new {@link Transformer} wrapping the provided components
     */
    public static <R, P, T> Transformer<R, P, T> of(
            SourceDeserializer<R, P> deserializer,
            SourceSplitter<P, T> splitter,
            SourceSerializer<T> serializer) {
        return new Transformer<>(deserializer, splitter, serializer);
    }
}
