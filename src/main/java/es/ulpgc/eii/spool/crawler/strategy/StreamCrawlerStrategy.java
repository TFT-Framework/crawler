package es.ulpgc.eii.spool.crawler.strategy;

import es.ulpgc.eii.spool.Event;
import es.ulpgc.eii.spool.EventBuffer;
import es.ulpgc.eii.spool.crawler.EventDeserializer;
import es.ulpgc.eii.spool.crawler.StreamSource;

import java.util.function.Consumer;
import java.util.stream.Stream;

public interface StreamCrawlerStrategy<T extends Event>
        extends CrawlerStrategy<T> {

    void start();
    void stop();

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
        private final StreamSource<R>        source;
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
            EventBuffer<T> buffer = new EventBuffer<>();
            Consumer<T> handler = event -> { buffer.push(event); onEvent.accept(event); };
            return new StreamCrawlerStrategy<T>() {
                @Override public void start()      { source.start(raw -> handler.accept(deserializer.deserialize(raw)), onError); }
                @Override public void stop()       { source.stop(); }
                @Override public Stream<T> crawl() { return buffer.drain(); }
            };
        }
    }
}

