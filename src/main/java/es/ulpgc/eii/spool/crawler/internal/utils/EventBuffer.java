package es.ulpgc.eii.spool.crawler.internal.utils;

import es.ulpgc.eii.spool.core.model.DomainEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

public final class EventBuffer<T extends DomainEvent> {
    private final Queue<T> buffer = new ConcurrentLinkedQueue<>();

    private EventBuffer() {}

    public static <T extends DomainEvent> EventBuffer<T> initialize() {
        return new EventBuffer<>();
    }

    public void push(T event) {
        buffer.add(event);
    }

    public Stream<T> drain() {
        List<T> snapshot = new ArrayList<>();
        T item;
        while ((item = buffer.poll()) != null) snapshot.add(item);
        return snapshot.stream();
    }
}
