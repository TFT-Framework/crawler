package es.ulpgc.eii.spool.crawler.builder;

import es.ulpgc.eii.spool.crawler.api.PlatformEventSource;

public abstract class BaseSourceStep<R, SELF extends BaseSourceStep<R, SELF>> {

    protected PlatformEventSource platformBus = e -> {};

    @SuppressWarnings("unchecked")
    public SELF withPlatformBus(PlatformEventSource platformBus) {
        this.platformBus = platformBus;
        return (SELF) this;
    }
}
