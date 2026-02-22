package es.ulpgc.eii.spool.crawler.builder;

import es.ulpgc.eii.spool.core.model.*;
import es.ulpgc.eii.spool.crawler.api.PlatformEventSource;
import es.ulpgc.eii.spool.crawler.api.source.StreamSource;
import es.ulpgc.eii.spool.crawler.api.EventSource;
import es.ulpgc.eii.spool.crawler.api.strategy.StreamCrawlerStrategy;
import es.ulpgc.eii.spool.crawler.internal.utils.EventBuffer;
import es.ulpgc.eii.spool.crawler.api.EventDeserializer;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class StreamCrawlerBuilder<R, T extends DomainEvent> extends BufferedCrawlerBuilder<R, T, StreamCrawlerBuilder<R, T>> {
    private final StreamSource<R> source;

    public StreamCrawlerBuilder(PlatformEventSource platformBus, StreamSource<R> source, EventDeserializer<R, T> deserializer) {
        super(platformBus, deserializer);
        this.source = source;
    }

    public StreamCrawlerStrategy<T> createSource() {
        EventBuffer<T> buffer = EventBuffer.initialize();
        Consumer<R> handler = consumerWith(buffer, SourceType.STREAM);
        AtomicBoolean closed = new AtomicBoolean(true);
        return new StreamCrawlerStrategy<T>() {
            @Override
            public Stream<T> collect() {
                if (closed.get()) throw new IllegalStateException("Call open() first");
                return buffer.drain();
            }

            @Override
            public EventSource<T> open() {
                if (!closed.get()) throw new IllegalStateException("Already open");
                platformBus.emit(SourceStarted.of(SourceType.STREAM));
                source.start(handler, onError);
                closed.set(false);
                return this;
            }

            @Override
            public void close() {
                source.stop();
                platformBus.emit(SourceStopped.of(SourceType.STREAM, null));
                closed.set(true);
            }
        };
    }
}