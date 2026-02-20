package es.ulpgc.eii.spool.crawler;

public interface EventInbox<R> {
    void receive(R event);
}
