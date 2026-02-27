package software.spool.crawler.api.source;

import software.spool.crawler.api.Source;
import software.spool.crawler.api.exception.SpoolException;

public interface PullSource<R> extends Source {
    R poll() throws SpoolException;
    default PullSource<R> open()  { return this; }
    String sourceId();
}
