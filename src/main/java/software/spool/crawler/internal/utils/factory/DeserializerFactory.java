package software.spool.crawler.internal.utils.factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import software.spool.crawler.internal.port.SourceDeserializer;
import software.spool.core.exception.DeserializationException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DeserializerFactory {
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public static SourceDeserializer<String, JsonNode> json() {
        return raw -> {
            try {
                return jsonMapper.readTree(raw);
            } catch (Exception e) {
                throw new DeserializationException("Failed to deserialize JSON", e);
            }
        };
    }

    public static SourceDeserializer<String, JsonNode> jsonArray() {
        return raw -> {
            try {
                JsonNode root = jsonMapper.readTree(raw);
                if (!root.isArray())
                    throw new DeserializationException("Expected JSON array", raw);
                return root;
            } catch (Exception e) {
                throw new DeserializationException("Failed to deserialize JSON array", e);
            }
        };
    }

    public static SourceDeserializer<String, JsonNode> yaml() {
        return raw -> {
            try {
                return yamlMapper.readTree(raw);
            } catch (Exception e) {
                throw new DeserializationException("Failed to deserialize YAML", e);
            }
        };
    }

    public static SourceDeserializer<String, JsonNode> yamlArray() {
        return raw -> {
            try {
                JsonNode root = yamlMapper.readTree(raw);
                if (!root.isArray())
                    throw new DeserializationException("Expected YAML array", raw);
                return root;
            } catch (Exception e) {
                throw new DeserializationException("Failed to deserialize YAML array", e);
            }
        };
    }

    public static SourceDeserializer<String, List<String>> textLines() {
        return raw -> {
            try {
                return Stream.of(raw.split("\n"))
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .toList();
            } catch (Exception e) {
                throw new DeserializationException("Failed to deserialize text lines", e);
            }
        };
    }

    public static SourceDeserializer<String, List<Map<String, String>>> csv() {
        return raw -> {
            try {
                String[] headers = raw.split("\n")[0].split(",");
                return Stream.of(raw.split("\n"))
                        .skip(1)
                        .map(line -> {
                            String[] values = line.split(",");
                            Map<String, String> row = new LinkedHashMap<>();
                            for (int i = 0; i < Math.min(headers.length, values.length); i++)
                                row.put(headers[i].trim(), values[i].trim());
                            return row;
                        })
                        .toList();
            } catch (Exception e) {
                throw new DeserializationException("Failed to deserialize CSV", e);
            }
        };
    }

    public static <T> SourceDeserializer<T, T> identity() {
        return raw -> raw;
    }
}
