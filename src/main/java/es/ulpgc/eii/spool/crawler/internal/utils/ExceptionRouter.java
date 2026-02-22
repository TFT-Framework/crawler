package es.ulpgc.eii.spool.crawler.internal.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ExceptionRouter {

    private record Entry<E extends Exception>(Class<E> type, Consumer<E> handler) {
        @SuppressWarnings("unchecked")
        boolean tryHandle(Exception e) {
            if (type.isInstance(e)) { handler.accept((E) e); return true; }
            return false;
        }
    }

    private final List<Entry<?>> entries = new ArrayList<>();
    private Consumer<Exception> fallback = e -> {};

    public <E extends Exception> ExceptionRouter on(Class<E> type, Consumer<E> handler) {
        entries.add(new Entry<>(type, handler));
        return this;
    }

    public ExceptionRouter orElse(Consumer<Exception> fallback) {
        this.fallback = fallback;
        return this;
    }

    public void dispatch(Exception e) {
        entries.stream()
               .filter(entry -> entry.tryHandle(e))
               .findFirst()
               .orElseGet(() -> { fallback.accept(e); return null; });
    }
}
