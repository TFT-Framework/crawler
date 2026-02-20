package es.ulpgc.eii.spool.example.sagulpa.crawlers.webhook;

import es.ulpgc.eii.spool.domain.crawler.strategy.WebhookCrawlerStrategy;
import es.ulpgc.eii.spool.example.sagulpa.OccupancyRecord;
import es.ulpgc.eii.spool.example.sagulpa.ParkingEvent;
import es.ulpgc.eii.spool.example.sagulpa.ParkingEventMapper;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class SagulpaWebhookCrawlerTest {

    @Test
    void crawl_shouldReturnEventsReceivedViaWebhook() {
        ParkingEventMapper mapper = new ParkingEventMapper();

        WebhookCrawlerStrategy<OccupancyRecord, ParkingEvent> crawler = WebhookCrawlerStrategy
                .from(OccupancyRecord.class)
                .deserializeWith(new ParkingEventMapper())
                .onEvent(e -> e.parkingLotId().equals("guanarteme"))
                .build();

        // Simula los POST HTTP que llegaría desde el sistema externo
        crawler.receive(new OccupancyRecord(1, "guanarteme", 90,  Instant.now(), "OPEN"));
        crawler.receive(new OccupancyRecord(2, "puerto",     300, Instant.now(), "OPEN"));

        List<ParkingEvent> events = crawler.crawl().toList();

        assertEquals(2, events.size());
        assertTrue(events.stream().anyMatch(e -> e.parkingLotId().equals("guanarteme")));
    }

    @Test
    void crawl_shouldDrainBufferOnEachCall() {
        WebhookCrawlerStrategy<OccupancyRecord, ParkingEvent> crawler = WebhookCrawlerStrategy
                .from(OccupancyRecord.class)
                .deserializeWith(new ParkingEventMapper())
                .onEvent(e -> e.parkingLotId().equals("guanarteme"))
                .build();

        ParkingEventMapper mapper = new ParkingEventMapper();

        crawler.receive(new OccupancyRecord(1, "elder", 50, Instant.now(), "OPEN"));

        List<ParkingEvent> firstCrawl  = crawler.crawl().toList();
        List<ParkingEvent> secondCrawl = crawler.crawl().toList();

        assertEquals(1, firstCrawl.size());
        assertTrue(secondCrawl.isEmpty()); // el buffer se vacía tras cada crawl()
    }
}
