package es.ulpgc.eii.spool.crawler.strategy;

import es.ulpgc.eii.spool.core.model.DomainEvent;

import java.util.stream.Stream;

public interface EventSource<T extends DomainEvent> extends AutoCloseable {
    Stream<T> collect();

    default EventSource<T> open() {return this;}
    default void close() {}
}
