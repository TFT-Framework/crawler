package software.spool.crawler.internal.strategy;

import software.spool.core.exception.*;
import software.spool.crawler.api.strategy.BaseCrawlerStrategy;
import software.spool.crawler.api.strategy.CrawlerStrategy;
import software.spool.crawler.internal.utils.CrawlerPorts;
import software.spool.crawler.internal.utils.factory.Transformer;
import software.spool.core.model.*;
import software.spool.crawler.api.source.PollSource;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

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
 * {@link software.spool.crawler.internal.port.SourceDeserializer}.</li>
 * <li>Split the deserialized value into individual records via the configured
 * {@link software.spool.crawler.internal.port.SourceSplitter}.</li>
 * <li>For each record: serialize it, write it to the inbox, and emit
 * {@code SourceItemCaptured} and {@code InboxItemStored} events.</li>
 * <li>Route any {@link SpoolException} through the
 * {@link software.spool.crawler.api.ErrorRouter}
 * inherited from {@link BaseCrawlerStrategy}.</li>
 * </ol>
 *
 * <p>
 * Idempotency keys are derived by hashing {@code sourceId + ":" + payload}
 * with SHA-256, ensuring that identical records from the same source always
 * produce the same key.
 * </p>
 *
 * @param <R> the raw type produced by the {@link PollSource}
 * @param <T> the intermediate type after deserialization
 * @param <O> the individual record type produced by the splitter
 */
public class PollCrawlerStrategy<R, T, O> extends BaseCrawlerStrategy implements CrawlerStrategy {
    private final PollSource<R> source;
    private final Transformer<R, T, O> transformer;
    private final CrawlerPorts ports;
    private final String sender;

    /**
     * Constructs a new strategy with the given source, transformer, ports, and
     * sender name.
     *
     * @param source      the poll source to fetch data from; must not be
     *                    {@code null}
     * @param transformer the pipeline to apply to each fetched payload; must not be
     *                    {@code null}
     * @param ports       the ports (bus, inbox, error router) to use; must not be
     *                    {@code null}
     * @param sender      the logical sender name included in emitted events
     */
    public PollCrawlerStrategy(PollSource<R> source, Transformer<R, T, O> transformer, CrawlerPorts ports,
            String sender) {
        super(ports.bus(), source.sourceId(), sender, ports.errorRouter());
        this.source = source;
        this.transformer = transformer;
        this.ports = ports;
        this.sender = sender;
    }

    /**
     * Executes one complete poll cycle.
     *
     * <p>
     * Opens the source, polls it, applies the transformer pipeline, and writes
     * each resulting record to the inbox. Exceptions are routed through the
     * inherited {@link software.spool.crawler.api.ErrorRouter}.
     * </p>
     *
     * @throws SpoolException if an unrecoverable error occurs
     */
    @Override
    public void execute() throws SpoolException {
        try (PollSource<R> source = this.source.open()) {
            transformer.splitter().split(transformer.deserializer().deserialize(source.poll()), source.sourceId())
                    .forEach(this::process);
        } catch (SpoolContextException e) {
            errorRouter.dispatch(e, e.context());
        } catch (Exception e) {
            errorRouter.dispatch(e);
        }
    }

    /**
     * Serializes one record, stores it in the inbox, and emits the corresponding
     * events.
     *
     * @param record the deserialized and split record to process
     */
    private void process(O record) {
        String payload = transformer.serializer().serialize(record, sender);
        SourceItemCaptured itemCapturedEvent = SourceItemCaptured.builder()
                .senderId(sender)
                .sourceId(source.sourceId())
                .idempotencyKey(generateIdempotencyKeyFrom(payload))
                .build();
        try {
            ports.inboxWriter().receive(payload, itemCapturedEvent.idempotencyKey());
            ports.bus().emit(itemCapturedEvent);
            ports.bus().emit(InboxItemStored.builder().from(itemCapturedEvent).build());
        } catch (Exception e) {
            throw new SpoolContextException(e, itemCapturedEvent);
        }
    }

    /**
     * Derives a deterministic idempotency key from the given payload.
     *
     * <p>
     * The key is a lowercase hex SHA-256 digest of
     * {@code sourceId + ":" + payload}.
     * </p>
     *
     * @param payload the serialized record payload
     * @return a 64-character hex string
     * @throws IllegalStateException if the SHA-256 algorithm is not available
     */
    private String generateIdempotencyKeyFrom(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = source.sourceId() + ":" + payload;
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
