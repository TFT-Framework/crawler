package software.spool.crawler.api.strategy;

import software.spool.model.RawDataWrittenToInbox;
import software.spool.model.RawDataReadFromSource;
import software.spool.model.SourceFailed;
import software.spool.crawler.api.EventBus;
import software.spool.crawler.api.SourceDeserializer;
import software.spool.crawler.api.SourceSerializer;
import software.spool.crawler.api.SourceSplitter;
import software.spool.crawler.api.source.Inbox;
import software.spool.crawler.api.source.InboxEntryId;
import software.spool.crawler.api.source.PullSource;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class PullCrawlerStrategy<R, T, O> implements CrawlerStrategy {
    private final PullSource<R> source;
    private final SourceDeserializer<R, T> deserializer;
    private final SourceSplitter<T, O> splitter;
    private final SourceSerializer<O> serializer;
    private final Inbox inbox;
    private final EventBus bus;

    public PullCrawlerStrategy(PullSource<R> source, SourceDeserializer<R, T> deserializer, SourceSplitter<T, O> splitter, SourceSerializer<O> serializer, Inbox inbox, EventBus bus) {
        this.source = source;
        this.deserializer = deserializer;
        this.splitter = splitter;
        this.serializer = serializer;
        this.inbox = inbox;
        this.bus = bus;

    }

    public void execute() {
        try (PullSource<R> source = this.source.open()) {
            R raw = source.poll();
            Stream<O> pepe = splitter.split(deserializer.deserialize(raw), source.sourceId());
            pepe.forEach(record -> {
                String wrap = serializer.wrap(record, source.sourceId());
                InboxEntryId id = inbox.receive(RawDataReadFromSource.builder().payload(wrap).sourceId(source.sourceId()).build());
                bus.emit(RawDataWrittenToInbox.from(source.sourceId())
                        .withIdempotencyKey(id.id())
                        .create());
            });
        }  catch (Exception e) {
            bus.emit(new SourceFailed(UUID.randomUUID().toString(), "SOURCE_FAILED", Instant.now(), Optional.of(e.getMessage())));
        }
    }
}
