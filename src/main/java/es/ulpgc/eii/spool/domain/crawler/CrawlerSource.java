package es.ulpgc.eii.spool.domain.crawler;

import java.util.stream.Stream;

public interface CrawlerSource<R> {
    Stream<R> read();
}