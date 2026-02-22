package es.ulpgc.eii.spool.crawler.api.strategy;

import es.ulpgc.eii.spool.core.model.*;
import es.ulpgc.eii.spool.crawler.api.EventSource;
import es.ulpgc.eii.spool.crawler.dsl.StreamSourceStep;
import es.ulpgc.eii.spool.crawler.api.source.StreamSource;

public interface StreamCrawlerStrategy<T extends DomainEvent> extends EventSource<T> {
    static <R> StreamSourceStep<R> from(StreamSource<R> source) {
        return new StreamSourceStep<>(source);
    }
}
