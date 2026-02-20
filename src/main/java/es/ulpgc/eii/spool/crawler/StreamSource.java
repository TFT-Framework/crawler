package es.ulpgc.eii.spool.crawler;

import java.util.function.Consumer;

public interface StreamSource<R> {
    void start(Consumer<R> onMessage, Consumer<Exception> onError);
    void stop();
}
