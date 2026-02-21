package es.ulpgc.eii.spool.crawler.strategy;

import es.ulpgc.eii.spool.core.model.Event;
import es.ulpgc.eii.spool.crawler.utils.EventBuffer;
import es.ulpgc.eii.spool.crawler.utils.EventDeserializer;
import es.ulpgc.eii.spool.crawler.source.StreamSource;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface StreamCrawlerStrategy<T extends Event> extends EventSource<T> {
    static <R> SourceStep<R> from(StreamSource<R> source) {
        return new SourceStep<>(source);
    }

    class SourceStep<R> {
        private final StreamSource<R> source;

        private SourceStep(StreamSource<R> source)
        {
            this.source = source;
        }

        public <T extends Event> StreamBuilder<R, T> deserializeWith(EventDeserializer<R, T> deserializer) {
            return new StreamBuilder<>(source, deserializer);
        }
    }

    class StreamBuilder<R, T extends Event> {
        private final StreamSource<R> source;
        private final EventDeserializer<R,T> deserializer;
        private Consumer<T> onEvent = event -> {};
        private Consumer<Exception> onError = error -> { throw new RuntimeException(error); };

        private StreamBuilder(StreamSource<R> source, EventDeserializer<R, T> deserializer) {
            this.source       = source;
            this.deserializer = deserializer;
        }

        public StreamBuilder<R, T> onEvent(Consumer<T> onEvent) {
            this.onEvent = onEvent;
            return this;
        }

        public StreamBuilder<R, T> onError(Consumer<Exception> onError) {
            this.onError = onError;
            return this;
        }

        public StreamCrawlerStrategy<T> build() {
            EventBuffer<T> buffer = EventBuffer.initialize();
            AtomicBoolean closed = new AtomicBoolean(true);
            Consumer<T> handler = event -> { buffer.push(event); onEvent.accept(event); };
            return new StreamCrawlerStrategy<T>() {
                @Override
                public Stream<T> collect() {
                    if (closed.get()) throw new IllegalStateException(
                            "EventSource must be opened before collecting. Call open() first."
                    );
                    return buffer.drain();
                }

                @Override public
                EventSource<T> open() {
                    if (!closed.get()) throw new IllegalStateException(
                            "EventSource is already open."
                    );
                    source.start(raw -> handler.accept(deserializer.deserialize(raw)), onError);
                    closed.set(false);
                    return this;
                }

                @Override
                public void close() {
                    source.stop();
                    closed.set(true);
                }
            };
        }
    }
}

