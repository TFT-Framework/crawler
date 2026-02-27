package software.spool.crawler.api;

import software.spool.crawler.api.exception.DeserializationException;

public interface SourceDeserializer<R, T> {
    T deserialize(R source) throws DeserializationException;
}
