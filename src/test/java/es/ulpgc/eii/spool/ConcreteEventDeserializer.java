package es.ulpgc.eii.spool;

import es.ulpgc.eii.spool.crawler.utils.EventDeserializer;

public class ConcreteEventDeserializer implements EventDeserializer<ConcreteExampleDTO, ConcreteExample> {
    @Override
    public ConcreteExample deserialize(ConcreteExampleDTO raw) {
        return new ConcreteExample(raw.id().toString());
    }
}
