package es.ulpgc.eii.spool.crawler.dsl;

import es.ulpgc.eii.spool.core.model.DomainEvent;
import es.ulpgc.eii.spool.crawler.builder.WebhookBuilder;
import es.ulpgc.eii.spool.crawler.api.PlatformEventSource;
import es.ulpgc.eii.spool.crawler.api.EventDeserializer;

public class WebhookSourceStep<R> {
    private PlatformEventSource platformBus = e -> {};

    public WebhookSourceStep<R> withPlatformBus(PlatformEventSource platformBus) {
        this.platformBus = platformBus;
        return this;
    }

    public <T extends DomainEvent> WebhookBuilder<R, T> deserializeWith(EventDeserializer<R, T> deserializer) {
        return new WebhookBuilder<>(platformBus, deserializer);
    }
}
