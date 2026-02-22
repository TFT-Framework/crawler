package es.ulpgc.eii.spool.crawler.api;

import es.ulpgc.eii.spool.core.model.DomainEvent;
import es.ulpgc.eii.spool.crawler.api.exception.DeserializationException;

@FunctionalInterface
public interface EventDeserializer<R, T extends DomainEvent> {
    T deserialize(R raw) throws DeserializationException;
}