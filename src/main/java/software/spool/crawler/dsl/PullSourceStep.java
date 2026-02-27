package software.spool.crawler.dsl;

import software.spool.crawler.api.EventBus;
import software.spool.crawler.api.SourceDeserializer;
import software.spool.crawler.api.SourceSerializer;
import software.spool.crawler.api.SourceSplitter;
import software.spool.crawler.api.source.Inbox;
import software.spool.crawler.api.source.PullSource;
import software.spool.crawler.api.strategy.CrawlerStrategy;
import software.spool.crawler.api.strategy.PullCrawlerStrategy;
import software.spool.crawler.internal.utils.ProcessorFormat;
import software.spool.crawler.internal.utils.Transformer;

public class PullSourceStep<R, T, O> {
    private final PullSource<R> source;
    private SourceDeserializer<R, T> deserializer;
    private SourceSplitter<T, O> splitter;
    private SourceSerializer<O> serializer;
    private Inbox inbox;
    private EventBus bus;

    public PullSourceStep(PullSource<R> source, Inbox inbox, EventBus bus) {
        this.source = source;
        this.inbox = inbox;
        this.bus = bus;
    }

    public PullSourceStep<R, T, O> splitWith(SourceSplitter<T, O> splitter) {
        this.splitter = splitter;
        return this;
    }

    public PullSourceStep<R, T, O> serializeWith(SourceSerializer<O> serializer) {
        this.serializer = serializer;
        return this;
    }

    public PullSourceStep<R, T, O> deserializeWith(SourceDeserializer<R, T> deserializer) {
        this.deserializer = deserializer;
        return this;
    }

    public PullSourceStep<R, T, O> inbox(Inbox inbox) {
        this.inbox = inbox;
        return this;
    }

    public PullSourceStep<R, T, O> bus(EventBus bus) {
        this.bus = bus;
        return this;
    }

    public CrawlerStrategy create() {
        return new PullCrawlerStrategy<>(source, deserializer, splitter, serializer, inbox, bus);
    }

    public <NT, NO> PullSourceStep<R, NT, NO> splitWith(
            ProcessorFormat<R, NT, NO> format) {

        Transformer<R, NT, NO> pipeline = format.pipeline();

        return new PullSourceStep<R, NT, NO>(source, inbox, bus)
                .deserializeWith(pipeline.deserializer())
                .splitWith(pipeline.splitter())
                .serializeWith(pipeline.serializer());
    }
}
