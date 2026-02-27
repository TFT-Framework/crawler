package software.spool.crawler.api;

import software.spool.crawler.api.exception.SpoolException;

public interface SourceSerializer<T> {
    String wrap(T record, String sourceId) throws SpoolException;
}
