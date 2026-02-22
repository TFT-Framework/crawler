package es.ulpgc.eii.spool.crawler.api.source;

import es.ulpgc.eii.spool.crawler.api.exception.SpoolException;

import java.util.function.Consumer;

public interface StreamSource<R> {
    void start(Consumer<R> onMessage, Consumer<Exception> onError) throws SpoolException;
    void stop();
}
