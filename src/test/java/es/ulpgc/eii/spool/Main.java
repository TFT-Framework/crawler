package es.ulpgc.eii.spool;

import es.ulpgc.eii.spool.crawler.strategy.EventSource;
import es.ulpgc.eii.spool.crawler.strategy.StreamCrawlerStrategy;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        try (EventSource<ConcreteExample> stream = StreamCrawlerStrategy
                .from(new ConcreteCrawlerSource())
                .withPlatformBus(System.out::println)
                .deserializeWith(new ConcreteEventDeserializer())
                .build()
        ) {
            stream.open().collect();
        }
    }
}
