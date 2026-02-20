package es.ulpgc.eii.spool.crawler.strategy;

import es.ulpgc.eii.spool.Event;
import es.ulpgc.eii.spool.EventBuffer;
import es.ulpgc.eii.spool.crawler.EventDeserializer;

import java.util.function.Consumer;
import java.util.stream.Stream;

public interface WebhookCrawlerStrategy<R, T extends Event> extends CrawlerStrategy<T> {

    void receive(R raw);

    static <R> WebhookSourceStep<R> from(Class<R> rawType) {
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

        private WebhookBuilder(EventDeserializer<R, T> deserializer) {
            this.deserializer = deserializer;
        }

        public WebhookBuilder<R, T> onEvent(Consumer<T> onEvent) {
            this.onEvent = onEvent;
            return this;
        }

        public WebhookCrawlerStrategy<R, T> build() {
            EventBuffer<T> buffer = EventBuffer.initialize();
            Consumer<T> handler = event -> { buffer.push(event); onEvent.accept(event); };
            return new WebhookCrawlerStrategy<R, T>() {
                @Override public void receive(R raw) { handler.accept(deserializer.deserialize(raw)); }
                @Override public Stream<T> crawl()   { return buffer.drain(); }
            };
        }
    }
}
