package es.ulpgc.eii.spool.crawler.dsl;

import es.ulpgc.eii.spool.core.model.DomainEvent;
import es.ulpgc.eii.spool.crawler.builder.PullCrawlerBuilder;
import es.ulpgc.eii.spool.crawler.api.source.CrawlerSource;
import es.ulpgc.eii.spool.crawler.api.PlatformEventSource;
import es.ulpgc.eii.spool.crawler.api.EventDeserializer;

public class PullSourceStep<R> {
    private final CrawlerSource<R> source;
    private PlatformEventSource platformBus = e -> {};

    public PullSourceStep(CrawlerSource<R> source) {
        this.source = source;
    }

    public PullSourceStep<R> withPlatformBus(PlatformEventSource platformBus) {
        this.platformBus = platformBus;
        return this;
    }

    public <T extends DomainEvent> PullCrawlerBuilder<R, T> deserializeWith(EventDeserializer<R, T> deserializer) {
        return new PullCrawlerBuilder<>(platformBus, source, deserializer);
    }
}