package software.spool.crawler.internal.utils;

import software.spool.crawler.api.source.Inbox;
import software.spool.crawler.api.source.InboxEntryId;
import software.spool.model.RawDataReadFromSource;

import java.lang.RuntimeException;
import java.sql.*;

public class JdbcInbox implements Inbox {

    private final String jdbcUrl = "jdbc:postgresql://localhost:5432/spool_db";
    private final String username = "spool_user";
    private final String password = "spool_pass";

    public JdbcInbox() {
    }

    @Override
    public InboxEntryId receive(RawDataReadFromSource event) throws RuntimeException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            conn.setAutoCommit(false);

            stmt = conn.prepareStatement(
                    "INSERT INTO spool_inbox.events (source, payload) VALUES (?, ?::jsonb) RETURNING id"
            );

            stmt.setString(1, event.sender());

            // Manejo correcto de Optional<String> para JSONB
            if (event.payload().isPresent() && !event.payload().get().trim().isEmpty()) {
                stmt.setString(2, event.payload().get());
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
                throw new RuntimeException("INSERT did not return an ID");
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw new RuntimeException("Failed to insert RawInboxEvent: " + e.getMessage(), e);
        } finally {
            // Cleanup en orden inverso
            if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException ignored) {}
            if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
        }
    }
}
