package software.spool.crawler.api.source;

import software.spool.crawler.internal.port.Source;
import software.spool.core.exception.SpoolException;

public interface PollSource<R> extends Source {
    R poll() throws SpoolException;
    default PollSource<R> open()  { return this; }
    String sourceId();
}
