package software.spool.crawler.api.source;

import software.spool.model.RawDataReadFromSource;
import software.spool.crawler.api.Source;
import software.spool.crawler.api.exception.SpoolException;

import java.util.function.Consumer;

public interface StreamSource<R> extends Source {
    void start(Consumer<RawDataReadFromSource> onMessage, Consumer<Exception> onError) throws SpoolException;
    void stop();
}
