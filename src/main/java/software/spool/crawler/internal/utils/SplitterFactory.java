package software.spool.crawler.internal.utils;

import com.fasterxml.jackson.databind.JsonNode;
import software.spool.crawler.api.SourceSplitter;
import software.spool.crawler.api.exception.SplitException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SplitterFactory {
    public static SourceSplitter<JsonNode, JsonNode> jsonArray() {
        return (parsed, source) -> {
            if (parsed.isArray()) {
                return StreamSupport.stream(parsed.spliterator(), false);
            }
            throw new SplitException("Expected JsonNode array");
        };
    }

    public static <T> SourceSplitter<T, T> single() {
        return (parsed, source) -> Stream.of(parsed);
    }

    public static SourceSplitter<ResultSet, Map<String, Object>> resultSet() {
        return (rs, source) -> StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        new ResultSetIterator(rs),
                        0
                ),
                false
        );
    }

    private static class ResultSetIterator implements Iterator<Map<String, Object>> {
        private final ResultSet rs;
        private Map<String, Object> nextRow = null;
        private boolean hasNext = false;

        public ResultSetIterator(ResultSet rs) {
            this.rs = rs;
        }

        @Override
        public boolean hasNext() {
            if (!hasNext) {
                try {
                    hasNext = rs.next();
                    if (hasNext) {
                        nextRow = rowToMap(rs);
                    }
                } catch (SQLException e) {
                    throw new SplitException("ResultSet hasNext failed", e);
                }
            }
            return hasNext;
        }

        @Override
        public Map<String, Object> next() {
            if (!hasNext) {
                throw new java.util.NoSuchElementException();
            }
            hasNext = false;
            return nextRow;
        }

        private Map<String, Object> rowToMap(ResultSet rs) throws SQLException {
            ResultSetMetaData meta = rs.getMetaData();
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                row.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            return row;
        }
    }
}
