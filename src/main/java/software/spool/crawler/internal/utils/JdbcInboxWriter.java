package software.spool.crawler.internal.utils;

import software.spool.core.exception.InboxWriteException;
import software.spool.crawler.api.port.InboxEntryId;
import software.spool.crawler.api.port.InboxWriter;
import software.spool.core.model.RawDataReadFromSource;

import java.sql.*;

public class JdbcInboxWriter implements InboxWriter {

    private final String jdbcUrl = "jdbc:postgresql://localhost:5432/spool_db";
    private final String username = "spool_user";
    private final String password = "spool_pass";

    public JdbcInboxWriter() {
    }

    @Override
    public InboxEntryId receive(RawDataReadFromSource event) throws InboxWriteException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            conn.setAutoCommit(false);

            stmt = conn.prepareStatement(
                    "INSERT INTO spool_inbox.events (source, payload) VALUES (?, ?::jsonb) RETURNING id");

            stmt.setString(1, event.sender());

            // Manejo correcto de Optional<String> para JSONB
            if (!event.payload().isEmpty()) {
                stmt.setString(2, event.payload());
            } else {
                stmt.setObject(2, null);
            }

            rs = stmt.executeQuery();

            if (rs.next()) {
                long entryId = rs.getLong("id");
                conn.commit();
                return new InboxEntryId(String.valueOf(entryId));
            } else {
                conn.rollback();
                throw new InboxWriteException("INSERT did not return an ID");
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw new InboxWriteException("Failed to insert RawInboxEvent: " + e.getMessage(), e);
        } finally {
            // Cleanup en orden inverso
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException ignored) {
                }
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException ignored) {
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
        }
    }
}
