package software.spool.crawler.internal.adapter.sql;

import software.spool.core.exception.SourcePollException;
import software.spool.core.exception.SpoolException;
import software.spool.crawler.api.port.source.PollSource;

import java.sql.*;
import java.util.List;
import java.util.Map;

public class SQLPollSource implements PollSource<ResultSet>, AutoCloseable {

    public enum DatabaseType {
        POSTGRESQL,
        MYSQL,
        MARIADB,
        SQLSERVER
    }

    private final String sourceId;
    private final DatabaseType databaseType;
    private final String host;
    private final int port;
    private final String databaseName;
    private final String username;
    private final String password;
    private final String query;
    private final List<Object> params;
    private final Map<String, String> properties;

    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;

    public SQLPollSource(
            String sourceId,
            DatabaseType databaseType,
            String host,
            int port,
            String databaseName,
            String username,
            String password,
            String query,
            List<Object> params,
            Map<String, String> properties
    ) {
        this.sourceId = sourceId;
        this.databaseType = databaseType;
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
        this.query = query;
        this.params = params;
        this.properties = properties;
    }

    @Override
    public ResultSet poll() throws SpoolException {
        try {
            closeCurrentResources();

            String jdbcUrl = buildJdbcUrl();
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            statement = connection.prepareStatement(query);

            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    statement.setObject(i + 1, params.get(i));
                }
            }

            resultSet = statement.executeQuery();
            return resultSet;

        } catch (SQLException e) {
            closeSilently();
            throw new SourcePollException(sourceId, e.getMessage());
        }
    }

    @Override
    public String sourceId() {
        return sourceId;
    }

    private String buildJdbcUrl() {
        String baseUrl = switch (databaseType) {
            case POSTGRESQL -> "jdbc:postgresql://%s:%d/%s".formatted(host, port, databaseName);
            case MYSQL -> "jdbc:mysql://%s:%d/%s".formatted(host, port, databaseName);
            case MARIADB -> "jdbc:mariadb://%s:%d/%s".formatted(host, port, databaseName);
            case SQLSERVER -> "jdbc:sqlserver://%s:%d;databaseName=%s"
                    .formatted(host, port, databaseName);
        };

        if (properties == null || properties.isEmpty()) {
            return baseUrl;
        }

        String separator = databaseType == DatabaseType.SQLSERVER ? ";" : "?";
        String joiner = databaseType == DatabaseType.SQLSERVER ? ";" : "&";

        String props = properties.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce((a, b) -> a + joiner + b)
                .orElse("");

        return baseUrl + separator + props;
    }

    @Override
    public void close() throws SpoolException {
        try {
            closeCurrentResources();
        } catch (SQLException e) {
            throw new SourcePollException(sourceId, "Error closing SQL resources for source '%s'"
                    .formatted(sourceId));
        }
    }

    private void closeCurrentResources() throws SQLException {
        if (resultSet != null && !resultSet.isClosed()) {
            resultSet.close();
        }
        if (statement != null && !statement.isClosed()) {
            statement.close();
        }
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }

        resultSet = null;
        statement = null;
        connection = null;
    }

    private void closeSilently() {
        try {
            closeCurrentResources();
        } catch (SQLException ignored) {
        }
    }
}