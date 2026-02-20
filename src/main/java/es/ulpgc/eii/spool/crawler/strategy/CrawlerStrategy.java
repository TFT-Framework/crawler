package es.ulpgc.eii.spool.crawler.strategy;

import es.ulpgc.eii.spool.Event;

import java.util.stream.Stream;

public interface CrawlerStrategy<T extends Event> {
    Stream<T> crawl();
}
