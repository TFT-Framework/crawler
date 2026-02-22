package es.ulpgc.eii.spool.crawler.builder;

import es.ulpgc.eii.spool.core.model.DomainEvent;
import es.ulpgc.eii.spool.crawler.api.PlatformEventSource;
import es.ulpgc.eii.spool.crawler.api.EventDeserializer;

import java.util.function.Consumer;

public abstract class BaseCrawlerBuilder<R, T extends DomainEvent, SELF extends BaseCrawlerBuilder<R, T, SELF>> {

    protected final PlatformEventSource platformBus;
    protected final EventDeserializer<R, T> deserializer;
    protected Consumer<T> onEvent = e -> {};
    protected Consumer<Exception> onError = e -> {};

    protected BaseCrawlerBuilder(PlatformEventSource platformBus, EventDeserializer<R, T> deserializer) {
        this.platformBus = platformBus;
        this.deserializer = deserializer;
    }

    @SuppressWarnings("unchecked")
    public SELF onEvent(Consumer<T> onEvent) { this.onEvent = onEvent; return (SELF) this; }

    @SuppressWarnings("unchecked")
    public SELF onError(Consumer<Exception> onError) { this.onError = onError; return (SELF) this; }

    protected static int getPayloadSize(Object raw) {
        return raw instanceof byte[] b ? b.length : raw.toString().length();
    }
}
