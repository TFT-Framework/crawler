package es.ulpgc.eii.spool.crawler;

import java.util.stream.Stream;

public interface CrawlerSource<R> {
    Stream<R> read();
}