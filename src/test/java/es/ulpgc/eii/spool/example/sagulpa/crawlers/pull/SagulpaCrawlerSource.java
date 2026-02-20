package es.ulpgc.eii.spool.example.sagulpa.crawlers.pull;

import es.ulpgc.eii.spool.domain.crawler.CrawlerSource;
import es.ulpgc.eii.spool.example.sagulpa.OccupancyRecord;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Stream;

public class SagulpaCrawlerSource implements CrawlerSource<OccupancyRecord> {
    private final Connection connection;

    public SagulpaCrawlerSource(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Stream<OccupancyRecord> read() {
        try {
            var rs = connection.createStatement()
                .executeQuery("SELECT * FROM occupancy ORDER BY recorded_at");
            return toStream(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Stream<OccupancyRecord> toStream(ResultSet rs) throws SQLException {
        var list = new ArrayList<OccupancyRecord>();
        while (rs.next()) {
            list.add(new OccupancyRecord(
                rs.getInt("record_id"),
                rs.getString("parking_lot_id"),
                rs.getInt("free_spots"),
                rs.getTimestamp("recorded_at").toInstant(),
                rs.getString("status")
            ));
        }
        return list.stream();
    }
}
