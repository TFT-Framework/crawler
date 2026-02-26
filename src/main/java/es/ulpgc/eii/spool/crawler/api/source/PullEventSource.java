package es.ulpgc.eii.spool.crawler.api.source;

import es.ulpgc.eii.spool.core.model.RawInboxEvent;
import es.ulpgc.eii.spool.crawler.api.EventSource;
import es.ulpgc.eii.spool.crawler.api.exception.SpoolException;

import java.util.stream.Stream;

public interface PullEventSource<R> extends EventSource {
    R poll() throws SpoolException;
    default PullEventSource<R> open()  { return this; }
}
