package software.spool.crawler.api.utils;

import com.fasterxml.jackson.databind.JsonNode;
import software.spool.core.port.serde.EnrichmentRule;
import software.spool.crawler.api.port.source.PollSource;
import software.spool.crawler.internal.utils.factory.Normalizer;
import software.spool.crawler.internal.utils.factory.NormalizerFactory;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Predefined {@link NormalizerFormat} constants ready to be used with the
 * crawler DSL.
 *
 * <p>
 * Each constant wires together a deserializer, a splitter and a serializer
 * via the corresponding
 * {@link NormalizerFactory}
 * factory method. Choose the constant that matches the raw data produced by
 * your {@link PollSource}.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>{@code
 * Crawlers.poll(mySource)
 *                 .withFormat(Formats.JSON_ARRAY)
 *                 .create();
 * }</pre>
 */
public final class StandardNormalizer {

        private StandardNormalizer() {
        }

        public static final NormalizerFormat<JsonNode, JsonNode, JsonNode> JSON_OBJECT = NormalizerFactory::jsonObject;
        public static final NormalizerFormat<JsonNode, JsonNode, JsonNode> JSON_ARRAY = NormalizerFactory::jsonArray;
        public static final NormalizerFormat<JsonNode, JsonNode, JsonNode> YAML_ARRAY = NormalizerFactory::yamlArray;
        public static final NormalizerFormat<ResultSet, Map<String, Object>, Map<String, Object>> RESULT_SET = (r, p) -> NormalizerFactory.resultSet();

        public static class Builder {
                private List<EnrichmentRule> enrichRules;
                private String rootPath;

                public Builder() {
                        this.enrichRules = new ArrayList<>();
                        this.rootPath = "";
                }

                public Builder enrichRules(List<EnrichmentRule> enrichRules) {
                        this.enrichRules = Objects.requireNonNullElse(enrichRules, new ArrayList<>());
                        return this;
                }

                public Builder rootPath(String rootPath) {
                        this.rootPath = Objects.requireNonNullElse(rootPath, "");
                        return this;
                }

                public Normalizer<?, ?, ?> valueOf(Format format) {
                        return switch (format) {
                                case JSON_OBJECT -> JSON_OBJECT.pipelineWith(enrichRules, rootPath);
                                case JSON_ARRAY  -> JSON_ARRAY.pipelineWith(enrichRules, rootPath);
                                case YAML_ARRAY  -> YAML_ARRAY.pipelineWith(enrichRules, rootPath);
                                case RESULT_SET  -> RESULT_SET.pipelineWith(enrichRules, rootPath);
                                default -> throw new IllegalArgumentException("Unknown format: " + format);
                        };
                }
        }

        public static enum Format {
                JSON_ARRAY, YAML_ARRAY, RESULT_SET, JSON_OBJECT
        }
}