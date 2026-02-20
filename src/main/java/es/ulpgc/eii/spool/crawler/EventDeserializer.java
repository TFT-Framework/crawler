package es.ulpgc.eii.spool.crawler;

import es.ulpgc.eii.spool.Event;

public interface EventDeserializer<R, T extends Event> {
    T deserialize(R raw);
}