package es.ulpgc.eii.spool.crawler.builder;

import es.ulpgc.eii.spool.core.model.*;
import es.ulpgc.eii.spool.crawler.api.PlatformEventSource;
import es.ulpgc.eii.spool.crawler.api.source.WebhookEventSource;
import es.ulpgc.eii.spool.crawler.internal.utils.EventBuffer;
import es.ulpgc.eii.spool.crawler.api.EventDeserializer;
import es.ulpgc.eii.spool.crawler.internal.utils.ExceptionRouter;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class WebhookCrawlerBuilder<R, T extends DomainEvent> extends BufferedCrawlerBuilder<R, T, WebhookCrawlerBuilder<R, T>> {
    private final ExceptionRouter errorRouter;

    public WebhookCrawlerBuilder(PlatformEventSource platformBus, EventDeserializer<R, T> deserializer) {
        super(platformBus, deserializer);
        this.errorRouter = buildRouter(SourceType.WEBHOOK);
    }

    //TODO handle errors
    public WebhookEventSource<R, T> createSource() {
        EventBuffer<T> buffer = EventBuffer.initialize();
        Consumer<R> handler = consumerWith(buffer, errorRouter, SourceType.WEBHOOK);
        return new WebhookEventSource<>() {
            @Override public void receive(R raw) {handler.accept(raw);}
            @Override public Stream<T> collect() {
                return buffer.drain();
            }
        };
    }
}