package software.spool.crawler.dsl;

import software.spool.crawler.api.source.PullSource;
import software.spool.crawler.internal.utils.InMemoryInbox;

public final class Crawlers {
    private Crawlers() {}

    public static <R> PullSourceStep<R, R, R> pull(PullSource<R> source) {
        return new PullSourceStep<>(source, new InMemoryInbox(), System.out::println);
    }
}
