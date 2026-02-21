package es.ulpgc.eii.spool.crawler.strategy;

import es.ulpgc.eii.spool.core.model.*;
import es.ulpgc.eii.spool.crawler.source.CrawlerSource;
import es.ulpgc.eii.spool.crawler.source.PlatformEventSource;
import es.ulpgc.eii.spool.crawler.utils.EventDeserializer;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface PullCrawlerStrategy<T extends DomainEvent> extends EventSource<T> {

    static <R> SourceStep<R> from(CrawlerSource<R> source) {
        return new SourceStep<>(source);
    }

    class SourceStep<R> {
        private final CrawlerSource<R> source;
        private PlatformEventSource platformBus;
        private Consumer<Exception> onError = error -> { throw new RuntimeException(error); };

        private SourceStep(CrawlerSource<R> source) {
            this.source = source;
            this.platformBus = e -> {};
        }

        public SourceStep<R> onError(Consumer<Exception> onError) {
            this.onError = onError;
            return this;
        }

        public SourceStep<R> withPlatformBus(PlatformEventSource platformBus) {
            this.platformBus = platformBus;
            return this;
        }

        public <T extends DomainEvent> EventHandlerBuilder<R, T> deserializeWith(EventDeserializer<R, T> deserializer) {
            return new EventHandlerBuilder<>(platformBus, source, deserializer, onError);
        }
    }

    class EventHandlerBuilder<R, T extends DomainEvent> {
        private final PlatformEventSource platformBus;
        private final CrawlerSource<R> source;
        private final EventDeserializer<R, T> deserializer;
        private final Consumer<Exception> onError;
        private Consumer<T> onEvent = event -> {};

        private EventHandlerBuilder(PlatformEventSource platformBus, CrawlerSource<R> source, EventDeserializer<R, T> deserializer, Consumer<Exception> onError) {
            this.platformBus = platformBus;
            this.source = source;
            this.deserializer = deserializer;
            this.onError = onError;
        }

        public EventHandlerBuilder<R, T> onEvent(Consumer<T> onEvent) {
            this.onEvent = onEvent;
            return this;
        }

        public PullCrawlerStrategy<T> build() {
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
}
