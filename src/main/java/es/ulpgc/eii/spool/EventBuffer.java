package es.ulpgc.eii.spool;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

/**
 * A thread-safe, in-memory buffer that accumulates events from a crawler
 * strategy and delivers them as a {@link Stream} on demand.
 *
 * <p>{@code EventBuffer} is the glue between the ingestion side (e.g. a Kafka
 * consumer or a webhook endpoint) and the processing side. Events are pushed
 * concurrently from any thread and drained atomically in a single batch.</p>
 *
 * <p>Internally backed by a {@link ConcurrentLinkedQueue}, making it safe for
 * concurrent producers without external synchronization.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * EventBuffer<ParkingEvent> buffer = EventBuffer.initialize();
 *
 * // Producer thread
 * buffer.push(new ParkingEvent(...));
 *
 * // Consumer thread (e.g. scheduled task)
 * buffer.drain().forEach(event -> process(event));
 * }</pre>
 *
 * @param <T> the type of {@link Event} held in this buffer
 * @see Event
 * @since 1.0.0
 */
public final class EventBuffer<T extends Event> {

    private final Queue<T> buffer = new ConcurrentLinkedQueue<>();

    /**
     * Creates a new, empty {@code EventBuffer}.
     *
     * <p>Prefer the static factory method {@link #initialize()} for a more
     * expressive instantiation at call sites.</p>
     *
     * @param <T> the type of {@link Event} to buffer
     * @return a new empty {@code EventBuffer} instance
     */
    public static <T extends Event> EventBuffer<T> initialize() {
        return new EventBuffer<>();
    }

    /**
     * Adds an event to the tail of the buffer.
     *
     * <p>This method is thread-safe and non-blocking. Multiple producer
     * threads may call it concurrently without external synchronization.</p>
     *
     * @param event the event to enqueue; must not be {@code null}
     */
    public void push(T event) {
        buffer.add(event);
    }

    /**
     * Atomically drains all currently buffered events and returns them as a
     * sequential {@link Stream}.
     *
     * <p>Events are removed from the buffer as they are collected into a
     * snapshot, so each event is delivered exactly once. Any events pushed
     * after {@code drain()} begins will remain in the buffer for the next call.</p>
     *
     * @return a {@link Stream} containing all events that were in the buffer
     *         at the time of the call; never {@code null}, may be empty
     */
    public Stream<T> drain() {
        List<T> snapshot = new ArrayList<>();
        T item;
        while ((item = buffer.poll()) != null) snapshot.add(item);
        return snapshot.stream();
    }
}
