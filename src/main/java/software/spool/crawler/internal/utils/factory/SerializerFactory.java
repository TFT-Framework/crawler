package software.spool.crawler.internal.utils.factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.spool.crawler.internal.port.SourceSerializer;
import software.spool.core.exception.SerializationException;

import java.util.Map;

/**
 * Factory methods for common {@link SourceSerializer} implementations backed
 * by a shared Jackson {@link ObjectMapper}.
 *
 * <p>
 * All returned serializers are stateless and thread-safe.
 * </p>
 */
public class SerializerFactory {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Returns a serializer that writes a {@link JsonNode} to a compact JSON string.
     *
     * @return a serializer for {@link JsonNode} values
     */
    public static SourceSerializer<JsonNode> jsonNode() {
        return (node, source) -> {
            try {
                return mapper.writeValueAsString(node);
            } catch (Exception e) {
                throw new SerializationException(node.toString(), e);
            }
        };
    }

    /**
     * Returns a serializer that writes a {@code Map<String, Object>} to a
     * compact JSON string.
     *
     * @return a serializer for map values
     */
    public static SourceSerializer<Map<String, Object>> map() {
        return (map, source) -> {
            try {
                return mapper.writeValueAsString(map);
            } catch (Exception e) {
                throw new SerializationException(map.toString(), e);
            }
        };
    }
}
