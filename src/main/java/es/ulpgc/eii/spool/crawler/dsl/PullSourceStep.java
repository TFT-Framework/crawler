package es.ulpgc.eii.spool.crawler.dsl;

import es.ulpgc.eii.spool.crawler.api.PlatformEventSource;
import es.ulpgc.eii.spool.crawler.api.SourceSplitter;
import es.ulpgc.eii.spool.crawler.api.source.EventInbox;
import es.ulpgc.eii.spool.crawler.api.source.PullEventSource;
import es.ulpgc.eii.spool.crawler.api.strategy.CrawlerStrategy;
import es.ulpgc.eii.spool.crawler.api.strategy.PullCrawlerStrategy;
import es.ulpgc.eii.spool.crawler.internal.utils.InMemoryInbox;

public class PullSourceStep<R> {
    private final PullEventSource<R> source;
    private SourceSplitter<R> splitter;
    private EventInbox inbox;
    private PlatformEventSource bus;

    public PullSourceStep(PullEventSource<R> source, InMemoryInbox inMemoryInbox) {
        this.source = source;
    }

    public PullSourceStep<R> splitWith(SourceSplitter<R> splitter) {
        this.splitter = splitter;
        return this;
    }

    public PullSourceStep<R> inbox(EventInbox inbox) {
        this.inbox = inbox;
        return this;
    }

    public PullSourceStep<R> bus(PlatformEventSource bus) {
        this.bus = bus;
        return this;
    }

    public CrawlerStrategy create() {
        return new PullCrawlerStrategy<>(source, splitter, inbox, bus);
    }
}