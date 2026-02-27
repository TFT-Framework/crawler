package software.spool.crawler.internal.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.spool.crawler.api.SourceSerializer;
import software.spool.crawler.api.exception.SerializationException;

import java.util.Map;

public class SerializerFactory {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static SourceSerializer<JsonNode> jsonNode() {
        return (node, source) -> {
            try {
                return mapper.writeValueAsString(node);
            } catch (Exception e) {
                throw new SerializationException("Failed to serialize JsonNode to payload", e);
            }
        };
    }

    public static SourceSerializer<Map<String, Object>> map() {
        return (map, source) -> {
            try {
                return mapper.writeValueAsString(map);
            } catch (Exception e) {
                throw new SerializationException("Failed to serialize Map to payload", e);
            }
        };
    }
}
