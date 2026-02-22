package es.ulpgc.eii.spool.crawler.api.source;

import es.ulpgc.eii.spool.crawler.api.exception.SpoolException;

import java.util.stream.Stream;

@FunctionalInterface
public interface CrawlerSource<R> {
    Stream<R> read() throws SpoolException;
}