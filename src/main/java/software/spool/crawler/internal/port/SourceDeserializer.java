package software.spool.crawler.internal.port;

import software.spool.core.exception.DeserializationException;

public interface SourceDeserializer<R, T> {
    T deserialize(R source) throws DeserializationException;
}
