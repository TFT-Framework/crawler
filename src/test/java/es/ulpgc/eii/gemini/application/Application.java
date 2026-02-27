package es.ulpgc.eii.gemini.application;

import es.ulpgc.eii.gemini.application.crawler.GeminiTradeCrawlerSource;
import software.spool.crawler.api.source.Inbox;
import software.spool.crawler.api.strategy.CrawlerStrategy;
import software.spool.crawler.dsl.Crawlers;
import software.spool.crawler.internal.utils.Formats;
import software.spool.crawler.internal.utils.JdbcInbox;

public class Application {
    private final CrawlerStrategy crawler;

    public Application() {
        Inbox inbox = new JdbcInbox();
        this.crawler = Crawlers.pull(new GeminiTradeCrawlerSource())
                .splitWith(Formats.JSON_ARRAY)
                .inbox(inbox)
                .create();
    }

    public void run() {
        crawler.execute();
    }
}
