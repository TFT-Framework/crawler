package es.ulpgc.eii.spool;

import es.ulpgc.eii.spool.crawler.strategy.EventSource;
import es.ulpgc.eii.spool.crawler.strategy.StreamCrawlerStrategy;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        try (EventSource<ConcreteExample> stream = StreamCrawlerStrategy
                .from(new ConcreteCrawlerSource())
                .deserializeWith(new ConcreteEventDeserializer())
                .onEvent(e -> System.out.println("Event received at " + LocalDateTime.now()))
                .onError(e -> System.out.println("Error on: " + e + " at " + LocalDateTime.now()))
                .build()
        ) {
            stream.open().collect().forEach(System.out::println);
        }
    }
}
