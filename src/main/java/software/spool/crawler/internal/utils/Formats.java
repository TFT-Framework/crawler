package software.spool.crawler.internal.utils;

import com.fasterxml.jackson.databind.JsonNode;

import java.sql.ResultSet;
import java.util.Map;

public final class Formats {

    public static final ProcessorFormat<String, JsonNode, JsonNode> JSON_ARRAY =
            TransformerFactory::jsonArray;

    public static final ProcessorFormat<ResultSet, ResultSet, Map<String,Object>> RESULT_SET =
            TransformerFactory::resultSet;

    public static final ProcessorFormat<String, JsonNode, JsonNode> YAML_ARRAY =
            TransformerFactory::yamlArray;
}