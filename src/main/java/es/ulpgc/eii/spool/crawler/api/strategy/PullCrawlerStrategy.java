package es.ulpgc.eii.spool.crawler.api.strategy;

import es.ulpgc.eii.spool.core.model.*;
import es.ulpgc.eii.spool.crawler.api.EventSource;
import es.ulpgc.eii.spool.crawler.dsl.PullSourceStep;
import es.ulpgc.eii.spool.crawler.api.source.CrawlerSource;

public interface PullCrawlerStrategy<T extends DomainEvent> extends EventSource<T> {
    static <R> PullSourceStep<R> from(CrawlerSource<R> source) {
        return new PullSourceStep<>(source);
    }
}
