package es.ulpgc.eii.spool.domain.crawler;

import es.ulpgc.eii.spool.domain.Event;

public interface StreamCrawlerStrategy<T extends Event>
        extends CrawlerStrategy<T>, EventListener<T> {
}
