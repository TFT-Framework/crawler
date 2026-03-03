package software.spool.crawler.api;

import com.fasterxml.jackson.databind.JsonNode;
import software.spool.crawler.internal.utils.factory.TransformerFactory;

import java.sql.ResultSet;
import java.util.Map;

/**
 * Predefined {@link ProcessorFormat} constants ready to be used with the
 * crawler DSL.
 *
 * <p>
 * Each constant wires together a deserializer, a splitter and a serializer
 * via the corresponding
 * {@link software.spool.crawler.internal.utils.factory.TransformerFactory}
 * factory method. Choose the constant that matches the raw data produced by
 * your {@link software.spool.crawler.api.source.PollSource}.
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
        public static final ProcessorFormat<String, JsonNode, JsonNode> JSON_ARRAY = TransformerFactory::jsonArray;

        /**
         * Pipeline for JDBC {@link ResultSet} sources.
         *
         * <p>
         * Passes the {@code ResultSet} through as-is (no deserialization step),
         * splits each row into a {@code Map<String, Object>} keyed by column label,
         * and serializes each row map to a JSON {@code String}.
         * </p>
         */
        public static final ProcessorFormat<ResultSet, ResultSet, Map<String, Object>> RESULT_SET = TransformerFactory::resultSet;

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
        public static final ProcessorFormat<String, JsonNode, JsonNode> YAML_ARRAY = TransformerFactory::yamlArray;
}