package es.ulpgc.eii.spool.domain.crawler.strategy;

import es.ulpgc.eii.spool.domain.Event;

import java.util.stream.Stream;

public interface CrawlerStrategy<T extends Event> {
    Stream<T> crawl();
}
