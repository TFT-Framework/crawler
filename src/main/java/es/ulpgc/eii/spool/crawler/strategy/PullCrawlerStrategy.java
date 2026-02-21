package es.ulpgc.eii.spool.crawler.strategy;

import es.ulpgc.eii.spool.core.model.Event;
import es.ulpgc.eii.spool.crawler.source.CrawlerSource;
import es.ulpgc.eii.spool.crawler.utils.EventDeserializer;

import java.util.function.Consumer;
import java.util.stream.Stream;

public interface PullCrawlerStrategy<T extends Event> extends EventSource<T> {

    static <R> SourceStep<R> from(CrawlerSource<R> source) {
        return new SourceStep<>(source);
    }

    class SourceStep<R> {
        private final CrawlerSource<R> source;
        private Consumer<Exception> onError = error -> { throw new RuntimeException(error); };

        private SourceStep(CrawlerSource<R> source) {
            this.source = source;
        }

        public SourceStep<R> onError(Consumer<Exception> onError) {
            this.onError = onError;
            return this;
        }

        public <T extends Event> EventHandlerBuilder<R, T> deserializeWith(EventDeserializer<R, T> deserializer) {
            return new EventHandlerBuilder<>(source, deserializer, onError);
        }
    }

    class EventHandlerBuilder<R, T extends Event> {
        private final CrawlerSource<R> source;
        private final EventDeserializer<R, T> deserializer;
        private final Consumer<Exception> onError;
        private Consumer<T> onEvent = event -> {};

        private EventHandlerBuilder(CrawlerSource<R> source,
                                    EventDeserializer<R, T> deserializer,
                                    Consumer<Exception> onError) {
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
                try {
                    return source.read()
                            .map(deserializer::deserialize)
                            .peek(onEvent);
                } catch (Exception e) {
                    onError.accept(e);
                    return Stream.empty();
                }
            };
        }
    }
}
