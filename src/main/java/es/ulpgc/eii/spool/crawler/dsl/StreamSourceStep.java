package es.ulpgc.eii.spool.crawler.dsl;

import es.ulpgc.eii.spool.core.model.DomainEvent;
import es.ulpgc.eii.spool.crawler.builder.StreamCrawlerBuilder;
import es.ulpgc.eii.spool.crawler.api.PlatformEventSource;
import es.ulpgc.eii.spool.crawler.api.source.StreamSource;
import es.ulpgc.eii.spool.crawler.api.EventDeserializer;

public class StreamSourceStep<R> {
    private final StreamSource<R> source;
    private PlatformEventSource platformBus = e -> {};

    public StreamSourceStep(StreamSource<R> source) {
        this.source = source;
    }

    public StreamSourceStep<R> withPlatformBus(PlatformEventSource platformBus) {
        this.platformBus = platformBus;
        return this;
    }

    public <T extends DomainEvent> StreamCrawlerBuilder<R, T> deserializeWith(EventDeserializer<R, T> deserializer) {
        return new StreamCrawlerBuilder<>(platformBus, source, deserializer);
    }
}
