package software.spool.crawler.internal.strategy;

import software.spool.core.exception.*;
import software.spool.crawler.api.port.InboxEntryId;
import software.spool.crawler.api.strategy.BaseCrawlerStrategy;
import software.spool.crawler.api.strategy.CrawlerStrategy;
import software.spool.crawler.internal.utils.CrawlerPorts;
import software.spool.crawler.internal.utils.factory.Transformer;
import software.spool.core.model.*;
import software.spool.crawler.api.source.PollSource;

import java.util.stream.Stream;

public class PollCrawlerStrategy<R, T, O> extends BaseCrawlerStrategy implements CrawlerStrategy {
    private final PollSource<R> source;
    private final Transformer<R, T, O> transformer;
    private final CrawlerPorts ports;
    private final String sender;

    public PollCrawlerStrategy(PollSource<R> source, Transformer<R, T, O> transformer, CrawlerPorts ports,
            String sender) {
        super(ports.bus(), sender, ports.errorRouter());
        this.source = source;
        this.transformer = transformer;
        this.ports = ports;
        this.sender = sender;
    }

    @Override
    public void execute() throws SpoolException {
        try (PollSource<R> source = this.source.open()) {
            safeSplit(safeDeserialize(safePoll(source)), sender).forEach(r -> safeProcess(r, sender));
        } catch (Exception e) {
            errorRouter.dispatch(e);
        }
    }

    private R safePoll(PollSource<R> source) {
        try {
            return source.poll();
        } catch (SourcePollException e) {
            throw e;
        } catch (Exception e) {
            throw new SourcePollException("Error while polling from source: " + source.sourceId(), e);
        }
    }

    private T safeDeserialize(R raw) {
        try {
            return transformer.deserializer().deserialize(raw);
        } catch (DeserializationException e) {
            throw e;
        } catch (Exception e) {
            throw new DeserializationException("Unexpected error in deserializer", e);
        }
    }

    private Stream<O> safeSplit(T deserialized, String sender) {
        try {
            return transformer.splitter().split(deserialized, sender);
        } catch (SourceSplitException e) {
            throw e;
        } catch (Exception e) {
            throw new SourceSplitException("Unexpected error in splitter", String.valueOf(deserialized), e);
        }
    }

    private void safeProcess(O record, String sender) {
        try {
            String payload = safeSerialize(record, sender);
            ports.bus().emit(RawDataWrittenToInbox.from(sender)
                    .withIdempotencyKey(safeWrite(payload, sender).value())
                    .withPayload(payload)
                    .create());
        } catch (SpoolException e) {
            errorRouter.dispatch(e);
        }
    }

    private String safeSerialize(O record, String sender) {
        try {
            return transformer.serializer().serialize(record, sender);
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new SerializationException("Unexpected error in serializer", String.valueOf(record), e);
        }
    }

    private InboxEntryId safeWrite(String payload, String sender) {
        try {
            return ports.inboxWriter().receive(RawDataReadFromSource.builder()
                    .payload(payload)
                    .sender(this.sender)
                    .build());
        } catch (InboxWriteException e) {
            throw e;
        } catch (Exception e) {
            throw new InboxWriteException("Unexpected error writing to inbox", e);
        }
    }
}
