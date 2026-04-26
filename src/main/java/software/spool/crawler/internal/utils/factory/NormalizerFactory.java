package software.spool.crawler.internal.utils.factory;

import com.fasterxml.jackson.databind.JsonNode;
import software.spool.core.adapter.jackson.*;
import software.spool.core.port.serde.*;
import software.spool.crawler.api.port.PayloadSplitter;
import software.spool.crawler.api.utils.StandardNormalizer;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * Factory class that assembles pre-configured {@link Normalizer} instances for
 * common data formats.
 *
 * <p>
 * The factory methods are referenced directly from the constants in
 * {@link StandardNormalizer} as method references:
 * 
 * <pre>{@code
 * ProcessorFormat<String, JsonNode, JsonNode> JSON_ARRAY = NormalizerFactory::jsonArray;
 * }</pre>
 *
 * <p>
 * Custom transformers can be created with the generic variant.
 * </p>
 */
public class NormalizerFactory {

    private NormalizerFactory() {}

    public static Normalizer<JsonNode, JsonNode, JsonNode> jsonArray(List<EnrichmentRule> rules, String rootPath) {
        return new Normalizer<>(
                PayloadDeserializerFactory.json().asNode(),
                PayloadExtractorFactory.withRules(rules),
                PayloadLocatorFactory.fromRootPath(rootPath),
                PayloadSplitterFactory.jsonArray(),
                RecordEnricherFactory.json(),
                RecordSerializerFactory.jsonNode());
    }

    public static Normalizer<JsonNode, JsonNode, JsonNode> yamlArray(List<EnrichmentRule> rules, String rootPath) {
        return new Normalizer<>(
                PayloadDeserializerFactory.yaml().asNode(),
                PayloadExtractorFactory.withRules(rules),
                PayloadLocatorFactory.fromRootPath(rootPath),
                PayloadSplitterFactory.jsonArray(),
                RecordEnricherFactory.json(),
                RecordSerializerFactory.jsonNode());
    }

    public static Normalizer<JsonNode, JsonNode, JsonNode> jsonObject(List<EnrichmentRule> rules, String rootPath) {
        return new Normalizer<>(
                PayloadDeserializerFactory.json().asNode(),
                PayloadExtractorFactory.withRules(rules),
                PayloadLocatorFactory.fromRootPath(rootPath),
                PayloadSplitterFactory.single(),
                RecordEnricherFactory.json(),
                RecordSerializerFactory.jsonNode());
    }

    public static Normalizer<ResultSet, Map<String, Object>, Map<String, Object>> resultSet() {
        return new Normalizer<>(
                PayloadDeserializerFactory.<ResultSet>noOp(),
                PayloadExtractorFactory.<ResultSet, Map<String, Object>>noOp(),
                PayloadLocatorFactory.<ResultSet>noOp(),
                PayloadSplitterFactory.resultSet(),
                RecordEnricherFactory.<Map<String, Object>>noOp(),
                RecordSerializerFactory.map());
    }

    public static <P, E, R> Normalizer<P, E, R> of(
            PayloadDeserializer<P> deserializer,
            PayloadExtractor<P, E> extractor,
            PayloadLocator<P> locator,
            PayloadSplitter<P, R> splitter,
            RecordEnricher<R, E> enricher,
            RecordSerializer<R> serializer) {
        return new Normalizer<>(deserializer, extractor, locator, splitter, enricher, serializer);
    }
}
