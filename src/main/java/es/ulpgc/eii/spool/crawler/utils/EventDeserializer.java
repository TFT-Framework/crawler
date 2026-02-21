package es.ulpgc.eii.spool.crawler.utils;

import es.ulpgc.eii.spool.core.model.DomainEvent;

@FunctionalInterface
public interface EventDeserializer<R, T extends DomainEvent> {
    T deserialize(R raw);
}