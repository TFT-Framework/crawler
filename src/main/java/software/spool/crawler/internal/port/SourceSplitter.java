package software.spool.crawler.internal.port;

import software.spool.core.exception.SpoolException;

import java.util.stream.Stream;

@FunctionalInterface
public interface SourceSplitter<I, O> {
    Stream<O> split(I payload, String sourceId) throws SpoolException;
}
