package software.spool.crawler.internal.utils;

import com.fasterxml.jackson.databind.JsonNode;
import software.spool.crawler.api.SourceDeserializer;
import software.spool.crawler.api.SourceSplitter;
import software.spool.crawler.api.SourceSerializer;

import java.sql.ResultSet;
import java.util.Map;

public class TransformerFactory {
    public static Transformer<String, JsonNode, JsonNode> jsonArray() {
        return new Transformer<>(
                DeserializerFactory.json(),
                SplitterFactory.jsonArray(),
                SerializerFactory.jsonNode()
        );
    }

    public static Transformer<String, JsonNode, JsonNode> yamlArray() {
        return new Transformer<>(
                DeserializerFactory.yamlArray(),
                SplitterFactory.jsonArray(),
                SerializerFactory.jsonNode()
        );
    }

    public static Transformer<ResultSet, ResultSet, Map<String, Object>> resultSet() {
        return new Transformer<>(
                r -> r,  // No-op para ResultSet
                SplitterFactory.resultSet(),
                SerializerFactory.map()
        );
    }

    public static <R, P, T> Transformer<R, P, T> of(
            SourceDeserializer<R, P> deserializer,
            SourceSplitter<P, T> splitter,
            SourceSerializer<T> serializer) {
        return new Transformer<>(deserializer, splitter, serializer);
    }
}
