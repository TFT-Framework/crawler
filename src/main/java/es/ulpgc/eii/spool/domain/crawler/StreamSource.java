package es.ulpgc.eii.spool.domain.crawler;

import java.util.function.Consumer;

public interface StreamSource<R> {
    void start(Consumer<R> onMessage, Consumer<Exception> onError);
    void stop();
}
