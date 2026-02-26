package es.ulpgc.eii.spool.crawler.builder;

import es.ulpgc.eii.spool.core.model.*;
import es.ulpgc.eii.spool.crawler.api.PlatformEventSource;
import es.ulpgc.eii.spool.crawler.api.SourceSplitter;
import es.ulpgc.eii.spool.crawler.internal.utils.ExceptionRouter;

import java.util.function.Consumer;

public abstract class BaseCrawlerBuilder<R, SELF extends BaseCrawlerBuilder<R, SELF>> {

    protected final PlatformEventSource platformBus;
    protected final SourceSplitter<> serializer;
    protected Consumer<T> onEvent = e -> {};
    protected Consumer<Exception> onError = e -> {};

    protected BaseCrawlerBuilder(PlatformEventSource platformBus, SourceSplitter<R> serializer) {
        this.platformBus = platformBus;
        this.serializer = serializer;
    }

    @SuppressWarnings("unchecked")
    public SELF onEvent(Consumer<T> onEvent) { this.onEvent = onEvent; return (SELF) this; }

    @SuppressWarnings("unchecked")
    public SELF onError(Consumer<Exception> onError) { this.onError = onError; return (SELF) this; }

    protected static int getPayloadSize(Object raw) {
        return raw instanceof byte[] b ? b.length : raw.toString().length();
    }

    protected ExceptionRouter buildRouter(SourceType sourceType) {
        return new ExceptionRouter();
        /*
                .on(DeserializationException.class, e -> {
                    platformBus.emit(EventDeserializationFailed.of(e.rawPayload(), e.getMessage(), sourceType));
                    onError.accept(e);
                })
                .on(DuplicateEventException.class, e ->
                        platformBus.emit(EventDuplicated.of(e.idempotencyKey(), sourceType)))
                .orElse(e -> {
                    platformBus.emit(SourceStopped.of(sourceType, e.getMessage()));
                    onError.accept(e);
                });
         */
    }
}
