package es.ulpgc.eii.spool.crawler.api;

import es.ulpgc.eii.spool.crawler.api.source.InboxEntryId;

public interface PlatformEventSource {
    void emit(InboxEntryId event);
}
