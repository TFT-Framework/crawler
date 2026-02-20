package es.ulpgc.eii.spool.domain.crawler.strategy;

import es.ulpgc.eii.spool.domain.Event;
import es.ulpgc.eii.spool.domain.crawler.CrawlerSource;
import es.ulpgc.eii.spool.domain.crawler.EventDeserializer;

import java.util.stream.Stream;

public interface PullCrawlerStrategy<T extends Event> extends CrawlerStrategy<T> {
    Stream<T> fetch();

    @Override
    default Stream<T> crawl() {
        return fetch();
    }

    public static <R> SourceStep<R> from(CrawlerSource<R> source) {
        return new SourceStep<>(source);
    }

    public static class SourceStep<R> {
        private final CrawlerSource<R> source;

        private SourceStep(CrawlerSource<R> source) {
            this.source = source;
        }

        public <T extends Event> PullCrawlerStrategy<T> deserializeWith(EventDeserializer<R, T> deserializer) {
            return () -> source.read().map(deserializer::deserialize);
        }
    }
}

