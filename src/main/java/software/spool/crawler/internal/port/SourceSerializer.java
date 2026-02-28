package software.spool.crawler.internal.port;

import software.spool.core.exception.SpoolException;

public interface SourceSerializer<T> {
    String serialize(T record, String sourceId) throws SpoolException;
}
