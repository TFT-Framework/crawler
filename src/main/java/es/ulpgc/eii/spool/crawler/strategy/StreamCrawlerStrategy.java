package es.ulpgc.eii.spool.crawler.strategy;

import es.ulpgc.eii.spool.core.model.*;
import es.ulpgc.eii.spool.crawler.source.PlatformEventSource;
import es.ulpgc.eii.spool.crawler.utils.EventBuffer;
import es.ulpgc.eii.spool.crawler.utils.EventDeserializer;
import es.ulpgc.eii.spool.crawler.source.StreamSource;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface StreamCrawlerStrategy<T extends DomainEvent> extends EventSource<T> {
    static <R> SourceStep<R> from(StreamSource<R> source) {
        return new SourceStep<>(source);
    }

    class SourceStep<R> {
        private final StreamSource<R> source;
        private PlatformEventSource platformBus;

        private SourceStep(StreamSource<R> source)
        {
            this.source = source;
            this.platformBus = e -> {};
        }

        public SourceStep<R> withPlatformBus(PlatformEventSource platformBus) {
            this.platformBus = platformBus;
            return this;
        }

        public <T extends DomainEvent> StreamBuilder<R, T> deserializeWith(EventDeserializer<R, T> deserializer) {
            return new StreamBuilder<>(platformBus, source, deserializer);
        }
    }

    class StreamBuilder<R, T extends DomainEvent> {
        private final PlatformEventSource platformBus;
        private final StreamSource<R> source;
        private final EventDeserializer<R,T> deserializer;
        private Consumer<T> onEvent = event -> {};
        private Consumer<Exception> onError = error -> {};

        private StreamBuilder(PlatformEventSource platformBus, StreamSource<R> source, EventDeserializer<R, T> deserializer) {
            this.platformBus = platformBus;
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
            Consumer<R> rawHandler = consumerWith(buffer);
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
                    source.start(rawHandler, onError);
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

        private static int getPayloadSize(Object raw) {
            return raw instanceof byte[] b ? b.length : raw.toString().length();
        }

        private Consumer<R> consumerWith(EventBuffer<T> buffer) {
            Set<String> seenIdempotencyKeys = ConcurrentHashMap.newKeySet();
            return r -> {
                try {
                    T event = deserializer.deserialize(r);
                    if (!seenIdempotencyKeys.add(event.idempotencyKey())) {
                        platformBus.emit(EventDuplicated.of(event.idempotencyKey(), SourceType.STREAM));
                        return;
                    }
                    platformBus.emit(EventReceived.of(event.correlationId(), SourceType.STREAM, getPayloadSize(r)));
                    buffer.push(event);
                    onEvent.accept(event);
                } catch (Exception e) {
                    platformBus.emit(EventDeserializationFailed.of(r.toString(), e.getMessage(), SourceType.STREAM));
                    onError.accept(e);
                }
            };
        }
    }
}

