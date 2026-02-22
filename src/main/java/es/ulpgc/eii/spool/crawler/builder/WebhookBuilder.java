package es.ulpgc.eii.spool.crawler.builder;

import es.ulpgc.eii.spool.core.model.*;
import es.ulpgc.eii.spool.crawler.api.PlatformEventSource;
import es.ulpgc.eii.spool.crawler.api.strategy.WebhookCrawlerStrategy;
import es.ulpgc.eii.spool.crawler.internal.utils.EventBuffer;
import es.ulpgc.eii.spool.crawler.api.EventDeserializer;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class WebhookBuilder<R, T extends DomainEvent>
        extends BufferedCrawlerBuilder<R, T, WebhookBuilder<R, T>> {

    public WebhookBuilder(PlatformEventSource platformBus, EventDeserializer<R, T> deserializer) {
        super(platformBus, deserializer);
    }

    public WebhookCrawlerStrategy<R, T> build() {
        EventBuffer<T> buffer = EventBuffer.initialize();
        Consumer<R> handler = consumerWith(buffer, SourceType.WEBHOOK);
        return new WebhookCrawlerStrategy<>() {
            @Override public void receive(R raw) {handler.accept(raw);}
            @Override public Stream<T> collect() {
                return buffer.drain();
            }
        };
    }
}