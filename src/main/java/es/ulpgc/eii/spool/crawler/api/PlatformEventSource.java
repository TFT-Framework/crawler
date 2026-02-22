package es.ulpgc.eii.spool.crawler.api;

import es.ulpgc.eii.spool.core.model.PlatformEvent;

public interface PlatformEventSource {
    void emit(PlatformEvent event);
}
