package es.ulpgc.eii.spool.crawler.api.strategy;

import es.ulpgc.eii.spool.core.model.RawInboxEvent;
import es.ulpgc.eii.spool.crawler.api.PlatformEventSource;
import es.ulpgc.eii.spool.crawler.api.SourceSplitter;
import es.ulpgc.eii.spool.crawler.api.source.EventInbox;
import es.ulpgc.eii.spool.crawler.api.source.InboxEntryId;
import es.ulpgc.eii.spool.crawler.api.source.PullEventSource;

import java.util.stream.Stream;

public class PullCrawlerStrategy<R> implements CrawlerStrategy {
    private final PullEventSource<R> source;
    private final SourceSplitter<R> splitter;
    private final EventInbox inbox;
    private final PlatformEventSource bus;

    public PullCrawlerStrategy(PullEventSource<R> source, SourceSplitter<R> splitter, EventInbox inbox, PlatformEventSource bus) {
        this.source = source;
        this.splitter = splitter;
        this.inbox = inbox;
        this.bus = bus;
    }

    public void execute() {
        Stream<RawInboxEvent> pepe = splitter.split(source.poll(), "pepe");
        pepe.forEach(record -> {
            InboxEntryId receive = inbox.receive(record);
            bus.emit(receive);
        });
    }
}
