package es.ulpgc.eii.spool.crawler.builder;

import es.ulpgc.eii.spool.core.model.*;
import es.ulpgc.eii.spool.crawler.api.PlatformEventSource;
import es.ulpgc.eii.spool.crawler.internal.utils.EventBuffer;
import es.ulpgc.eii.spool.crawler.api.EventDeserializer;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class BufferedCrawlerBuilder<R, T extends DomainEvent, SELF extends BufferedCrawlerBuilder<R, T, SELF>>
        extends BaseCrawlerBuilder<R, T, SELF> {

    protected BufferedCrawlerBuilder(PlatformEventSource platformBus, EventDeserializer<R, T> deserializer) {
        super(platformBus, deserializer);
    }

    protected Consumer<R> consumerWith(EventBuffer<T> buffer, SourceType sourceType) {
        Set<String> seenKeys = ConcurrentHashMap.newKeySet();
        return r -> {
            try {
                T event = deserializer.deserialize(r);
                if (!seenKeys.add(event.idempotencyKey())) {
                    platformBus.emit(EventDuplicated.of(event.idempotencyKey(), sourceType));
                    return;
                }
                platformBus.emit(EventReceived.of(event.idempotencyKey(), sourceType, getPayloadSize(r)));
                buffer.push(event);
                onEvent.accept(event);
            } catch (Exception e) {
                platformBus.emit(EventDeserializationFailed.of(r.toString(), e.getMessage(), sourceType));
                onError.accept(e);
            }
        };
    }
}
