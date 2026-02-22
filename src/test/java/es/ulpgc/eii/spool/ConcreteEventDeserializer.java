package es.ulpgc.eii.spool;

import es.ulpgc.eii.spool.crawler.api.EventDeserializer;

public class ConcreteEventDeserializer implements EventDeserializer<ConcreteExampleDTO, ConcreteExample> {
    @Override
    public ConcreteExample deserialize(ConcreteExampleDTO raw) {
        if (raw.id().toString().endsWith("f")) {
            throw new RuntimeException("Simulated deserialization error for: " + raw.id());
        }
        return new ConcreteExample(raw.id().toString());
    }
}
