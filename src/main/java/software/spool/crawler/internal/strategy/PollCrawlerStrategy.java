package software.spool.crawler.internal.strategy;

import software.spool.core.exception.*;
import software.spool.crawler.api.port.InboxEntryId;
import software.spool.crawler.api.strategy.BaseCrawlerStrategy;
import software.spool.crawler.api.strategy.CrawlerStrategy;
import software.spool.crawler.internal.utils.CrawlerPorts;
import software.spool.crawler.internal.utils.factory.Transformer;
import software.spool.core.model.*;
import software.spool.crawler.api.source.PollSource;

public class PollCrawlerStrategy<R, T, O> extends BaseCrawlerStrategy implements CrawlerStrategy {
    private final PollSource<R> source;
    private final Transformer<R, T, O> transformer;
    private final CrawlerPorts ports;
    private final String sender;

    public PollCrawlerStrategy(PollSource<R> source, Transformer<R, T, O> transformer, CrawlerPorts ports, String sender) {
        super(ports.bus(), sender, ports.errorRouter());
        this.source = source;
        this.transformer = transformer;
        this.ports = ports;
        this.sender = sender;
    }

    @Override
    public void execute() throws SpoolException {
        try (PollSource<R> source = this.source.open()) {
            transformer.splitter().split(transformer.deserializer().deserialize(source.poll()), source.sourceId())
                    .forEach(e -> process(e, sender));
        } catch (Exception e) {
            errorRouter.dispatch(e);
        }
    }

    private void process(O record, String sender) {
        try {
            String payload = serialize(record, sender);
            ports.bus().emit(RawDataWrittenToInbox.from(sender)
                    .withIdempotencyKey(writeToInbox(payload).value())
                    .withPayload(payload)
                    .create());
        } catch (SpoolException e) {
            errorRouter.dispatch(e);
        }
    }

    private String serialize(O record, String sender) {
        return transformer.serializer().serialize(record, sender);
    }

    private InboxEntryId writeToInbox(String payload) {
        return ports.inboxWriter().receive(RawDataReadFromSource.builder()
                .payload(payload)
                .sender(sender)
                .build());
    }
}
