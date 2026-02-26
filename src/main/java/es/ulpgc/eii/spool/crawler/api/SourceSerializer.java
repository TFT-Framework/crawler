package es.ulpgc.eii.spool.crawler.api;

import es.ulpgc.eii.spool.core.model.RawInboxEvent;
import es.ulpgc.eii.spool.crawler.api.exception.SpoolException;

public interface SourceSerializer<T> {
    RawInboxEvent wrap(T record, String sourceId) throws SpoolException;
}
