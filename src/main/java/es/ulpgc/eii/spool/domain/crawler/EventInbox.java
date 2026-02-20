package es.ulpgc.eii.spool.domain.crawler;

import es.ulpgc.eii.spool.domain.Event;

public interface EventInbox<T extends Event> {
    void receive(T event);
}
