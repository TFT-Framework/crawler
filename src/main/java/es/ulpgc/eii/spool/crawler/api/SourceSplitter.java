package es.ulpgc.eii.spool.crawler.api;

import es.ulpgc.eii.spool.core.model.RawInboxEvent;
import es.ulpgc.eii.spool.crawler.api.exception.SpoolException;

import java.util.stream.Stream;

@FunctionalInterface
public interface SourceSplitter<R> {
    Stream<RawInboxEvent> split(R raw, String sourceId) throws SpoolException;
}
