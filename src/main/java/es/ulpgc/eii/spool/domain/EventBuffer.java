package es.ulpgc.eii.spool.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

public final class EventBuffer<T extends Event> {

    private final Queue<T> buffer = new ConcurrentLinkedQueue<>();

    public static <T extends Event> EventBuffer<T> initialize() {
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
