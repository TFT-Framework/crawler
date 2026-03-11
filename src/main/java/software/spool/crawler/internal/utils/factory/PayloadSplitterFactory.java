package software.spool.crawler.internal.utils.factory;

import com.fasterxml.jackson.databind.JsonNode;
import software.spool.core.exception.SourceSplitException;
import software.spool.crawler.api.port.PayloadSplitter;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Factory methods for common {@link PayloadSplitter} implementations.
 *
 * <p>
 * All returned splitters are stateless lambdas or inner-class instances
 * and can be safely reused across multiple executions.
 * </p>
 */
public class PayloadSplitterFactory {
    /**
     * Returns a splitter that streams the elements of a {@link JsonNode} array.
     *
     * @return a splitter for JSON arrays; throws {@link SourceSplitException} if
     *         the parsed node is not an array
     */
    public static PayloadSplitter<JsonNode, JsonNode> jsonArray() {
        return parsed -> {
            if (parsed.isArray())
                return StreamSupport.stream(parsed.spliterator(), false);
            throw new SourceSplitException("Expected JsonNode array, got: " + parsed.getNodeType().name(),
                    parsed.toString());
        };
    }

    /**
     * Returns a splitter that wraps a single parsed value in a one-element stream,
     * effectively performing no splitting.
     *
     * @param <T> the type of the value
     * @return a passthrough splitter
     */
    public static <T> PayloadSplitter<T, T> single() {
        return Stream::of;
    }

    /**
     * Returns a splitter that iterates over a JDBC {@link ResultSet} and projects
     * each row into a {@code Map<String, Object>} keyed by column label.
     *
     * @return a splitter for {@code ResultSet} sources
     */
    public static PayloadSplitter<ResultSet, Map<String, Object>> resultSet() {
        return rs -> StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        new ResultSetIterator(rs),
                        0),
                false);
    }

    /**
     * {@link Iterator} that lazily advances a {@link ResultSet} and converts each
     * row to a {@code Map<String, Object>}.
     */
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
                    throw new SourceSplitException("ResultSet hasNext failed", null);
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
