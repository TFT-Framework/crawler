package software.spool.crawler.internal.utils.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import software.spool.core.exception.DeserializationException;
import software.spool.core.port.PayloadDeserializer;

public final class DomainMapperFactory {
    private static final ObjectMapper CAMEL  = new ObjectMapper().findAndRegisterModules();
    private static final ObjectMapper SNAKE  = new ObjectMapper().findAndRegisterModules()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    private static final ObjectMapper PASCAL = new ObjectMapper().findAndRegisterModules()
            .setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE);
    private static final ObjectMapper KEBAB  = new ObjectMapper().findAndRegisterModules()
            .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

    public static <D> PayloadDeserializer<String, D> camelCase(Class<D> type) {
        return raw -> {
            try {
                return CAMEL.readValue(raw, type);
            } catch (JsonProcessingException e) {
                throw new DeserializationException(raw, e);
            }
        };
    }

    public static <D> PayloadDeserializer<String, D> snakeCase(Class<D> type) {
        return raw -> {
            try {
                return SNAKE.readValue(raw, type);
            } catch (JsonProcessingException e) {
                throw new DeserializationException(raw, e);
            }
        };
    }

    public static <D> PayloadDeserializer<String, D> pascalCase(Class<D> type) {
        return raw -> {
            try {
                return PASCAL.readValue(raw, type);
            } catch (JsonProcessingException e) {
                throw new DeserializationException(raw, e);
            }
        };
    }

    public static <D> PayloadDeserializer<String, D> kebabCase(Class<D> type) {
        return raw -> {
            try {
                return KEBAB.readValue(raw, type);
            } catch (JsonProcessingException e) {
                throw new DeserializationException(raw, e);
            }
        };
    }

    private DomainMapperFactory() {}
}


