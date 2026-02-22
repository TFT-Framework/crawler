package es.ulpgc.eii.spool;

import es.ulpgc.eii.spool.crawler.dsl.Crawlers;
import es.ulpgc.eii.spool.crawler.api.EventSource;

public class Main {
    public static void main(String[] args) {
        try (EventSource<ConcreteExample> stream = Crawlers
                .stream(new ConcreteCrawlerSource())
                .withPlatformBus(System.out::println)
                .deserializeWith(new ConcreteEventDeserializer())
                .build()
        ) {
            stream.open().collect();
        }
    }
}
