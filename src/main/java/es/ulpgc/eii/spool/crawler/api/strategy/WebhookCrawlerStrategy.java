package es.ulpgc.eii.spool.crawler.api.strategy;

import es.ulpgc.eii.spool.core.model.*;
import es.ulpgc.eii.spool.crawler.api.EventSource;
import es.ulpgc.eii.spool.crawler.dsl.WebhookSourceStep;
import es.ulpgc.eii.spool.crawler.api.source.EventInbox;

public interface WebhookCrawlerStrategy<R, T extends DomainEvent> extends EventSource<T>, EventInbox<R> {
    static <R> WebhookSourceStep<R> from() {return new WebhookSourceStep<>();}
}
