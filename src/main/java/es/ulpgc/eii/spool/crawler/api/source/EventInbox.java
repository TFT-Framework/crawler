package es.ulpgc.eii.spool.crawler.api.source;

import es.ulpgc.eii.spool.crawler.api.exception.SpoolException;

@FunctionalInterface
public interface EventInbox<R> {
    void receive(R event) throws SpoolException;
}
