package es.ulpgc.eii.spool.crawler.api.source;

@FunctionalInterface
public interface EventInbox<R> {
    void receive(R event);
}
