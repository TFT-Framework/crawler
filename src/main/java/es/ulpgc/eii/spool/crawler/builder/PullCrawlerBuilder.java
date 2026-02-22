package es.ulpgc.eii.spool.crawler.builder;

import es.ulpgc.eii.spool.core.model.*;
import es.ulpgc.eii.spool.crawler.api.source.CrawlerSource;
import es.ulpgc.eii.spool.crawler.api.PlatformEventSource;
import es.ulpgc.eii.spool.crawler.api.strategy.PullCrawlerStrategy;
import es.ulpgc.eii.spool.crawler.api.EventDeserializer;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class PullCrawlerBuilder<R, T extends DomainEvent> extends BaseCrawlerBuilder<R, T, PullCrawlerBuilder<R, T>> {
    private final CrawlerSource<R> source;

    public PullCrawlerBuilder(PlatformEventSource platformBus, CrawlerSource<R> source, EventDeserializer<R, T> deserializer) {
        super(platformBus, deserializer);
        this.source = source;
    }

    public PullCrawlerStrategy<T> createSource() {
        return () -> {
            platformBus.emit(SourceStarted.of(SourceType.PULL));
            Set<T> seen = new HashSet<>();
            return source.read()
                .flatMap(r -> {
                    try {
                        T event = deserializer.deserialize(r);
                        platformBus.emit(EventReceived.of(event.id(), SourceType.PULL, event.toString().length()));
                        if (!seen.add(event))
                            platformBus.emit(EventDuplicated.of(event.id(), SourceType.PULL));
                        onEvent.accept(event);
                        return Stream.of(event);
                    } catch (Exception e) {
                        platformBus.emit(EventDeserializationFailed.of(r.toString(), e.getMessage(), SourceType.PULL));
                        onError.accept(e);
                        return Stream.empty();
                    }
                });
        };
    }
}