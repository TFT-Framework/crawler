package software.spool.crawler.internal.utils.factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.spool.crawler.internal.port.SourceSerializer;
import software.spool.core.exception.SerializationException;

import java.util.Map;

public class SerializerFactory {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static SourceSerializer<JsonNode> jsonNode() {
        return (node, source) -> {
            try {
                return mapper.writeValueAsString(node);
            } catch (Exception e) {
                throw new SerializationException("Failed to serialize JsonNode to payload", node.toString(), e);
            }
        };
    }

    public static SourceSerializer<Map<String, Object>> map() {
        return (map, source) -> {
            try {
                return mapper.writeValueAsString(map);
            } catch (Exception e) {
                throw new SerializationException("Failed to serialize Map to payload", map.toString(), e);
            }
        };
    }
}
