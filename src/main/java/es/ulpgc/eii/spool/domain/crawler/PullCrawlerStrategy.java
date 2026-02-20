package es.ulpgc.eii.spool.domain.crawler;

import es.ulpgc.eii.spool.domain.Event;

import java.util.stream.Stream;

public interface PullCrawlerStrategy<T extends Event> extends CrawlerStrategy<T> {
    Stream<T> fetch();

    @Override
    default Stream<T> crawl() {
        return fetch();
    }
}

