package es.ulpgc.eii.spool.crawler.utils;

import es.ulpgc.eii.spool.core.model.Event;

@FunctionalInterface
public interface EventDeserializer<R, T extends Event> {
    T deserialize(R raw);
}