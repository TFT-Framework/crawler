package es.ulpgc.eii.spool.domain.crawler;

public interface EventInbox<R> {
    void receive(R event);
}
