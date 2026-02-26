package es.ulpgc.eii.spool.crawler.api;

public interface EventSource extends AutoCloseable {
    String sourceId();
    default void close() {}
}
