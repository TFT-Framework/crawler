package es.ulpgc.eii.spool;

import es.ulpgc.eii.spool.crawler.source.StreamSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ConcreteCrawlerSource implements StreamSource<ConcreteExampleDTO> {
    private final List<ConcreteExampleDTO> examples;

    public ConcreteCrawlerSource() {
        examples = new ArrayList<>();
        for (int i = 0; i < 20; i++)
            examples.add(new ConcreteExampleDTO(UUID.randomUUID()));
    }

    @Override
    public void start(Consumer<ConcreteExampleDTO> onMessage, Consumer<Exception> onError) {
        examples.forEach(e -> {
            try {
                if (e.id().toString().endsWith("f")) {
                    throw new RuntimeException("Simulated deserialization error for: " + e.id());
                }
                onMessage.accept(e);
            } catch (Exception ex) {
                onError.accept(ex);
            }
        });
    }

    @Override
    public void stop() {
    }
}
