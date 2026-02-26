package es.ulpgc.eii.spool.crawler.dsl;

import es.ulpgc.eii.spool.crawler.api.source.PullEventSource;
import es.ulpgc.eii.spool.crawler.internal.utils.InMemoryInbox;

public final class Crawlers {
    private Crawlers() {}

    public static <R> PullSourceStep<R> pull(PullEventSource<R> source) {
        return new PullSourceStep<>(source, new InMemoryInbox());
    }
}
