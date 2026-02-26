package es.ulpgc.eii.spool.crawler.builder;

import es.ulpgc.eii.spool.core.model.*;
import es.ulpgc.eii.spool.crawler.api.exception.DuplicateEventException;
import es.ulpgc.eii.spool.crawler.api.PlatformEventSource;
import es.ulpgc.eii.spool.crawler.api.strategy.PullCrawlerStrategy;
import es.ulpgc.eii.spool.crawler.api.EventDeserializer;
import es.ulpgc.eii.spool.crawler.internal.utils.ExceptionRouter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class PullCrawlerBuilder<R, T extends DomainEvent> extends BaseCrawlerBuilder<R, T, PullCrawlerBuilder<R, T>> {
    private final CrawlerSource<R> source;
    private final ExceptionRouter errorRouter;

    public PullCrawlerBuilder(PlatformEventSource platformBus, CrawlerSource<R> source, EventDeserializer<R, T> deserializer) {
        super(platformBus, deserializer);
        this.source = source;
        this.errorRouter = buildRouter(SourceType.PULL);
    }

    public PullCrawlerStrategy<T> createSource() {
        return () -> {
            platformBus.emit(SourceStarted.of(SourceType.PULL));
            List<T> events = readAll();
            platformBus.emit(SourceStopped.of(SourceType.PULL, null));
            return events.stream();
        };
    }

    private List<T> readAll() {
        Set<String> seenKeys = new HashSet<>();
        Stream<R> stream;
        try {
            stream = source.read();
        } catch (Exception e) {
            errorRouter.dispatch(e);
            return List.of();
        }
        return stream
                .flatMap(r -> toEvent(r, seenKeys))
                .toList();
    }

    private Stream<T> toEvent(R r, Set<String> seenKeys) {
        T event = deserializer.deserialize(r);
        if (!seenKeys.add(event.idempotencyKey())) throw new DuplicateEventException(event.idempotencyKey());
        platformBus.emit(EventReceived.of(event.correlationId(), SourceType.PULL, getPayloadSize(r)));
        onEvent.accept(event);
        return Stream.of(event);
    }
}