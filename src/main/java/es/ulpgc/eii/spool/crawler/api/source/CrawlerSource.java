package es.ulpgc.eii.spool.crawler.api.source;

import java.util.stream.Stream;

@FunctionalInterface
public interface CrawlerSource<R> {
    Stream<R> read();
}