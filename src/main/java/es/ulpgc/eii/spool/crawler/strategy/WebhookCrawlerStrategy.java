package es.ulpgc.eii.spool.crawler.strategy;

import es.ulpgc.eii.spool.core.model.Event;
import es.ulpgc.eii.spool.crawler.utils.EventBuffer;
import es.ulpgc.eii.spool.crawler.utils.EventDeserializer;
import es.ulpgc.eii.spool.crawler.source.EventInbox;

import java.util.function.Consumer;
import java.util.stream.Stream;

public interface WebhookCrawlerStrategy<R, T extends Event> extends EventSource<T>, EventInbox<R> {
    static <R> WebhookSourceStep<R> from() {
        return new WebhookSourceStep<>();
    }

    class WebhookSourceStep<R> {
        public <T extends Event> WebhookBuilder<R, T> deserializeWith(EventDeserializer<R, T> deserializer) {
            return new WebhookBuilder<>(deserializer);
        }
    }

    class WebhookBuilder<R, T extends Event> {
        private final EventDeserializer<R, T> deserializer;
        private Consumer<T> onEvent = event -> {};
        private Consumer<Exception> onError = e -> {};

        private WebhookBuilder(EventDeserializer<R, T> deserializer) {
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
            Consumer<T> handler = event -> { buffer.push(event); onEvent.accept(event); };
            return new WebhookCrawlerStrategy<>() {
                @Override
                public void receive(R raw) {
                    try {
                        handler.accept(deserializer.deserialize(raw));
                    } catch (Exception e) {
                        onError.accept(e);
                    }
                }

                @Override
                public Stream<T> collect() {
                    return buffer.drain();
                }
            };
        }
    }
}
