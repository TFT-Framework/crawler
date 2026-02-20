package es.ulpgc.eii.spool.example.sagulpa.crawlers.gemini;

import es.ulpgc.eii.spool.domain.crawler.strategy.PullCrawlerStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class GeminiCrawlerExampleTest {

    @Test
    void crawl_shouldFetchBitcoinTradesFromGeminiAPI() {
        PullCrawlerStrategy<BitcoinTradeEvent> crawler = PullCrawlerStrategy
                .from(new GeminiTradeCrawlerSource())
                .deserializeWith(new BitcoinTradeMapper());

        List<BitcoinTradeEvent> events = crawler.crawl().toList();

        assertFalse(events.isEmpty());
        events.forEach(e -> System.out.println(
                e.occurredAt() + " | " + e.tradeType() + " | $" + e.price() + " | " + e.amount() + " BTC"
        ));
    }
}
