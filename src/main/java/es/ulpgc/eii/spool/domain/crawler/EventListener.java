package es.ulpgc.eii.spool.domain.crawler;

import es.ulpgc.eii.spool.domain.Event;

public interface EventListener<T extends Event> {
    void on(T event);
    void on(Exception error);
}
