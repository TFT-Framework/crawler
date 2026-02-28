package software.spool.crawler.api.source;

import software.spool.core.model.RawDataReadFromSource;
import software.spool.crawler.internal.port.Source;
import software.spool.core.exception.SpoolException;

import java.util.function.Consumer;

public interface StreamSource<R> extends Source {
    void start(Consumer<RawDataReadFromSource> onMessage, Consumer<Exception> onError) throws SpoolException;
    void stop();
}
