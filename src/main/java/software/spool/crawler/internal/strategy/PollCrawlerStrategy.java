package software.spool.crawler.internal.strategy;

import software.spool.core.exception.*;
import software.spool.crawler.api.port.PayloadSplitter;
import software.spool.crawler.api.strategy.BaseCrawlerStrategy;
import software.spool.crawler.api.strategy.CrawlerStrategy;
import software.spool.crawler.api.utils.DomainEventMapping;
import software.spool.crawler.api.utils.CrawlerPorts;
import software.spool.crawler.internal.utils.factory.Transformer;
import software.spool.core.model.*;
import software.spool.crawler.api.port.source.PollSource;

import java.util.List;

/**
 * Concrete {@link CrawlerStrategy} that orchestrates a full
 * poll-deserialize-split-serialize
 * cycle and delivers each resulting record to the inbox and event bus.
 *
 * <p>
 * The execution flow performed by {@link #execute()} is:
 * </p>
 * <ol>
 * <li>Open the {@link PollSource} inside a try-with-resources block.</li>
 * <li>Call {@link PollSource#poll()} to fetch the raw payload.</li>
 * <li>Deserialize the raw payload via the configured
 * {@link software.spool.core.port.PayloadDeserializer}.</li>
 * <li>Split the deserialized value into individual records via the configured
 * {@link PayloadSplitter}.</li>
 * <li>For each record: serialize it, write it to the inbox, and emit
 * {@code SourceItemCaptured} and {@code InboxItemStored} events.</li>
 * <li>Route any {@link SpoolException} through the
 * {@link software.spool.core.utils.ErrorRouter}
 * inherited from {@link BaseCrawlerStrategy}.</li>
 * </ol>
 *
 * <p>
 * Idempotency keys are derived by hashing {@code sourceId + ":" + payload}
 * with SHA-256, ensuring that identical records from the same source always
 * produce the same key.
 * </p>
 *
 * @param <I> the raw type produced by the {@link PollSource}
 * @param <T> the intermediate type after deserialization
 * @param <O> the individual record type produced by the splitter
 */
public class PollCrawlerStrategy<I, T, O> implements CrawlerStrategy {
    private final PollSource<I> source;
    private final Transformer<I, T, O> transformer;
    private final CrawlerPorts ports;
    private final List<DomainEventMapping<?>> domainMappings;

    /**
     * Constructs a new strategy with the given source, transformer and ports.
     *
     * @param source      the poll source to fetch data from; must not be
     *                    {@code null}
     * @param transformer the pipeline to apply to each fetched payload; must not be
     *                    {@code null}
     * @param ports       the ports (bus, inbox, error router) to use; must not be
     *                    {@code null}
     */
    public PollCrawlerStrategy(PollSource<I> source, Transformer<I, T, O> transformer, CrawlerPorts ports) {
        this(source, transformer, ports, List.of());
    }

    public PollCrawlerStrategy(PollSource<I> source, Transformer<I, T, O> transformer, CrawlerPorts ports, List<DomainEventMapping<?>> domainMappings) {
        this.source = source;
        this.transformer = transformer;
        this.ports = ports;
        this.domainMappings = domainMappings;
    }

    /**
     * Executes one complete poll cycle.
     *
     * <p>
     * Opens the source, polls it, applies the transformer pipeline, and writes
     * each resulting record to the inbox. Exceptions are routed through the
     * inherited {@link software.spool.core.utils.ErrorRouter}.
     * </p>
     *
     * @throws SpoolException if an unrecoverable error occurs
     */
    @Override
    public void execute() throws SpoolException {
        try (PollSource<I> source = this.source.open()) {
            transformer.splitter().split(transformer.deserializer().deserialize(source.poll()))
                    .forEach(this::process);
        } catch (SpoolContextException e) {
            ports.errorRouter().dispatch(e, e.context());
        } catch (Exception e) {
            ports.errorRouter().dispatch(e);
        }
    }

    /**
     * Serializes one record, stores it in the inbox, and emits the corresponding
     * events.
     *
     * @param record the deserialized and split record to process
     */
    private void process(O record) {
        String payload = transformer.serializer().serialize(record);
        SourceItemCaptured itemCapturedEvent = SourceItemCaptured.builder()
                .idempotencyKey(IdempotencyKey.of(source.sourceId(), payload))
                .build();
        try {
            ports.bus().emit(itemCapturedEvent);
            ports.inboxWriter().receive(payload, itemCapturedEvent.idempotencyKey());
            ports.bus().emit(InboxItemStored.builder().from(itemCapturedEvent).build());
            domainMappings.forEach(m ->
                    ports.bus().emit(m.resolve(payload, itemCapturedEvent.idempotencyKey())));
        } catch (Exception e) {
            throw new SpoolContextException(e, itemCapturedEvent);
        }
    }
}
