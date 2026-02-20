package es.ulpgc.eii.spool.domain.crawler;

import es.ulpgc.eii.spool.domain.Event;

public interface WebhookCrawlerStrategy<T extends Event>
        extends CrawlerStrategy<T>, EventInbox<T> {
}
