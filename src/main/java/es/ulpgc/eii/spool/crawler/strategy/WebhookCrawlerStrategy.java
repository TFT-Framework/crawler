package es.ulpgc.eii.spool.crawler.strategy;

import es.ulpgc.eii.spool.core.model.*;
import es.ulpgc.eii.spool.crawler.source.PlatformEventSource;
import es.ulpgc.eii.spool.crawler.utils.EventBuffer;
import es.ulpgc.eii.spool.crawler.utils.EventDeserializer;
import es.ulpgc.eii.spool.crawler.source.EventInbox;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface WebhookCrawlerStrategy<R, T extends DomainEvent> extends EventSource<T>, EventInbox<R> {
    static <R> SourceStep<R> from() {
        return new SourceStep<>();
    }

    class SourceStep<R> {
        private PlatformEventSource platformBus;

        public SourceStep<R> withPlatformBus(PlatformEventSource platformBus) {
            this.platformBus = platformBus;
            return this;
        }

        public <T extends DomainEvent> WebhookBuilder<R, T> deserializeWith(EventDeserializer<R, T> deserializer) {
            return new WebhookBuilder<>(platformBus, deserializer);
        }
    }

    class WebhookBuilder<R, T extends DomainEvent> {
        private final PlatformEventSource platformBus;
        private final EventDeserializer<R, T> deserializer;
        private Consumer<T> onEvent = event -> {};
        private Consumer<Exception> onError = e -> {};

        private WebhookBuilder(PlatformEventSource platformBus, EventDeserializer<R, T> deserializer) {
            this.platformBus = platformBus;
            this.deserializer = deserializer;
        }

        public WebhookBuilder<R, T> onEvent(Consumer<T> onEvent) {
            this.onEvent = onEvent;
            return this;
        }

        public WebhookBuilder<R, T> onError(Consumer<Exception> onError) {
            this.onError = onError;
            return this;
        }

        public WebhookCrawlerStrategy<R, T> build() {
            EventBuffer<T> buffer = EventBuffer.initialize();
            Consumer<R> handler = consumerWith(buffer);
            return new WebhookCrawlerStrategy<>() {
                @Override public void receive(R raw) {handler.accept(raw);}
                @Override public Stream<T> collect() {
                    return buffer.drain();
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
                        platformBus.emit(EventDuplicated.of(event.idempotencyKey(), SourceType.WEBHOOK));
                        return;
                    }
                    platformBus.emit(EventReceived.of(event.idempotencyKey(), SourceType.WEBHOOK, getPayloadSize(r)));
                    buffer.push(event);
                    onEvent.accept(event);
                } catch (Exception e) {
                    platformBus.emit(EventDeserializationFailed.of(r.toString(), e.getMessage(), SourceType.WEBHOOK));
                    onError.accept(e);
                }
            };
        }
    }
}
