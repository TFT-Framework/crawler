package es.ulpgc.eii.spool.crawler.source;

@FunctionalInterface
public interface EventInbox<R> {
    void receive(R event);
}
