package software.spool.crawler.example.gemini.application;

import software.spool.crawler.api.Crawler;
import software.spool.crawler.example.gemini.application.crawler.GeminiTradeCrawlerSource;
import software.spool.crawler.api.strategy.CrawlerStrategy;
import software.spool.crawler.api.builder.CrawlerBuilderFactory;
import software.spool.crawler.api.utils.Formats;

public class Application {
    private final Crawler crawler;

    public Application() {
        this.crawler = CrawlerBuilderFactory.poll(new GeminiTradeCrawlerSource())
                .withFormat(Formats.JSON_ARRAY)
                .create();
    }

    public void run() {
        crawler.startCrawling();
    }
}
