package es.ulpgc.eii.spool.domain.crawler;

import es.ulpgc.eii.spool.domain.Event;

public interface EventDeserializer<R, T extends Event> {
    T deserialize(R raw);
}