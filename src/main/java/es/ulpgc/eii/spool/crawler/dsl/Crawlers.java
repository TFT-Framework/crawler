package es.ulpgc.eii.spool.crawler.dsl;

import es.ulpgc.eii.spool.crawler.api.source.CrawlerSource;
import es.ulpgc.eii.spool.crawler.api.source.StreamSource;

public final class Crawlers {
    private Crawlers() {}

    public static <R> PullSourceStep<R> pull(CrawlerSource<R> source) {
        return new PullSourceStep<>(source);
    }
    public static <R> StreamSourceStep<R> stream(StreamSource<R> source) {
        return new StreamSourceStep<>(source);
    }
    public static <R> WebhookSourceStep<R> webhook() {
        return new WebhookSourceStep<>();
    }
}
