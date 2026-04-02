package software.spool.crawler.api.utils;

import com.fasterxml.jackson.databind.JsonNode;
import software.spool.crawler.api.port.source.PollSource;
import software.spool.crawler.internal.utils.factory.TransformerFactory;

import java.sql.ResultSet;
import java.util.Map;

/**
 * Predefined {@link TransformerFormat} constants ready to be used with the
 * crawler DSL.
 *
 * <p>
 * Each constant wires together a deserializer, a splitter and a serializer
 * via the corresponding
 * {@link software.spool.crawler.internal.utils.factory.TransformerFactory}
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
public final class Formats {

        private Formats() {
        }

        /**
         * Pipeline for raw JSON strings that represent a JSON array.
         *
         * <p>
         * Deserializes the {@code String} input into a {@link JsonNode},
         * splits each element of the array into individual {@link JsonNode} records,
         * and serializes each record back to a compact JSON {@code String}.
         * </p>
         */
        public static final TransformerFormat<JsonNode, JsonNode> JSON_ARRAY = TransformerFactory::jsonArray;

        public static final TransformerFormat<JsonNode, JsonNode> JSON_OBJECT = TransformerFactory::jsonObject;

        /**
         * Pipeline for JDBC {@link ResultSet} sources.
         *
         * <p>
         * Passes the {@code ResultSet} through as-is (no deserialization step),
         * splits each row into a {@code Map<String, Object>} keyed by column label,
         * and serializes each row map to a JSON {@code String}.
         * </p>
         */
        public static final TransformerFormat<ResultSet, Map<String, Object>> RESULT_SET = TransformerFactory::resultSet;

        /**
         * Pipeline for raw YAML strings that represent a YAML sequence (array).
         *
         * <p>
         * Deserializes the {@code String} input into a {@link JsonNode} tree
         * (using Jackson's YAML parser), splits each element of the sequence into
         * individual {@link JsonNode} records, and serializes each record to a
         * compact JSON {@code String}.
         * </p>
         */
        public static final TransformerFormat<JsonNode, JsonNode> YAML_ARRAY = TransformerFactory::yamlArray;

        @SuppressWarnings("unchecked")
        public static TransformerFormat<Object, Object> valueOf(String format) {
                return (TransformerFormat<Object, Object>) switch (format.toUpperCase()) {
                        case "JSON_ARRAY"  -> JSON_ARRAY;
                        case "JSON_OBJECT" -> JSON_OBJECT;
                        case "RESULT_SET"  -> RESULT_SET;
                        case "YAML_ARRAY"  -> YAML_ARRAY;
                        default -> throw new IllegalArgumentException("Unknown format: " + format);
                };
        }
}