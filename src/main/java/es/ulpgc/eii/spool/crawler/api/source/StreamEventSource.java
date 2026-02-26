package es.ulpgc.eii.spool.crawler.api.source;

import es.ulpgc.eii.spool.core.model.RawInboxEvent;
import es.ulpgc.eii.spool.crawler.api.EventSource;
import es.ulpgc.eii.spool.crawler.api.exception.SpoolException;

import java.util.function.Consumer;

public interface StreamEventSource<R> extends EventSource {
    void start(Consumer<RawInboxEvent> onMessage, Consumer<Exception> onError) throws SpoolException;
    void stop();
}
