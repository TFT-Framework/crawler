package es.ulpgc.eii.spool.example.sagulpa.crawlers.pull;

import es.ulpgc.eii.spool.domain.crawler.strategy.PullCrawlerStrategy;
import es.ulpgc.eii.spool.example.sagulpa.ParkingEvent;
import es.ulpgc.eii.spool.example.sagulpa.ParkingEventMapper;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class SagulpaCrawlerExampleTest {

    @Test
    void crawl_shouldFetchParkingEventsFromPostgres() throws Exception {
        var connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/spool_db",
                "spool_user", "spool_pass"
        );

        PullCrawlerStrategy<ParkingEvent> crawler = PullCrawlerStrategy
                .from(new SagulpaCrawlerSource(connection))
                .deserializeWith(new ParkingEventMapper());

        List<ParkingEvent> events = crawler.crawl().toList();
        assertFalse(events.isEmpty());
        events.forEach(e -> System.out.println(e.parkingLotId() + " → " + e.freeSpots()));
    }
}
