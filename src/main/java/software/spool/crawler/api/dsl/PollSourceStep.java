package software.spool.crawler.api.dsl;

import software.spool.crawler.api.ErrorRouter;
import software.spool.crawler.api.ProcessorFormat;
import software.spool.crawler.api.port.EventBusEmitter;
import software.spool.crawler.api.port.InboxWriter;
import software.spool.crawler.api.source.PollSource;
import software.spool.crawler.api.strategy.CrawlerStrategy;
import software.spool.crawler.internal.decorator.*;
import software.spool.crawler.internal.strategy.PollCrawlerStrategy;
import software.spool.crawler.internal.utils.CrawlerPorts;
import software.spool.crawler.internal.utils.factory.Transformer;

import java.util.Optional;
import java.util.UUID;

/**
 * Fluent builder that configures a poll-based {@link CrawlerStrategy}.
 *
 * <p>
 * Instances are normally obtained via
 * {@link software.spool.crawler.api.dsl.Crawlers#poll(PollSource)}
 * and then further customised with the chainable setter methods before calling
 * {@link #create()} to build the strategy.
 * </p>
 *
 * <p>
 * All ports passed to the setter methods are automatically wrapped in their
 * corresponding {@code Safe*} decorators to normalise unchecked exceptions into
 * typed {@link software.spool.core.exception.SpoolException} subclasses.
 * </p>
 *
 * @param <R> the raw type returned by the configured {@link PollSource}
 * @param <T> the intermediate type after deserialization
 * @param <O> the individual record type produced by the splitter
 */
public class PollSourceStep<R, T, O> {
    private final PollSource<R> source;
    private Transformer<R, T, O> transformer;
    private CrawlerPorts ports;
    private String sender;

    /**
     * Creates a new step wrapping the given source and ports.
     *
     * <p>
     * The source is immediately decorated with {@link SafePollSource} to
     * normalise any unchecked exceptions thrown during polling.
     * </p>
     *
     * @param source the poll source; must not be {@code null}
     * @param ports  the initial set of ports to use; must not be {@code null}
     */
    public PollSourceStep(PollSource<R> source, CrawlerPorts ports) {
        this.source = SafePollSource.of(source);
        this.ports = ports;
    }

    /**
     * Sets the processing {@link Transformer} (deserializer + splitter +
     * serializer).
     *
     * <p>
     * Each component of the transformer is wrapped in its corresponding
     * {@code Safe*} decorator before being stored.
     * </p>
     *
     * @param transformer the transformer to apply to each polled payload;
     *                    must not be {@code null}
     * @return this step for chaining
     */
    public PollSourceStep<R, T, O> transformer(Transformer<R, T, O> transformer) {
        this.transformer = Transformer.of(
                SafeSourceDeserializer.of(transformer.deserializer()),
                SafeSourceSplitter.of(transformer.splitter()),
                SafeSourceSerializer.of(transformer.serializer()));
        return this;
    }

    /**
     * Replaces all ports with the given {@link CrawlerPorts} bundle.
     *
     * @param ports the new ports; must not be {@code null}
     * @return this step for chaining
     */
    public PollSourceStep<R, T, O> ports(CrawlerPorts ports) {
        this.ports = ports;
        return this;
    }

    /**
     * Sets the logical name used as {@code senderId} in emitted events.
     *
     * <p>
     * If not set, a random name of the form {@code PollCrawler-<uuid8>}
     * is generated at {@link #create()} time.
     * </p>
     *
     * @param sender the sender name; must not be {@code null}
     * @return this step for chaining
     */
    public PollSourceStep<R, T, O> senderName(String sender) {
        this.sender = sender;
        return this;
    }

    /**
     * Overrides the {@link EventBusEmitter} port.
     *
     * <p>
     * The provided emitter is wrapped in {@link SafeEventBusEmitterEmitter}.
     * </p>
     *
     * @param bus the event bus emitter; must not be {@code null}
     * @return this step for chaining
     */
    public PollSourceStep<R, T, O> bus(EventBusEmitter bus) {
        this.ports = getPorts(bus, ports.inboxWriter(), ports.errorRouter());
        return this;
    }

    /**
     * Overrides the {@link InboxWriter} port.
     *
     * <p>
     * The provided writer is wrapped in {@link SafeInboxWriter}.
     * </p>
     *
     * @param inbox the inbox writer; must not be {@code null}
     * @return this step for chaining
     */
    public PollSourceStep<R, T, O> inbox(InboxWriter inbox) {
        this.ports = getPorts(ports.bus(), inbox, ports.errorRouter());
        return this;
    }

    /**
     * Overrides the {@link ErrorRouter} used to handle exceptions during execution.
     *
     * @param errorRouter the error router; if {@code null}, the default routing
     *                    table from
     *                    {@link software.spool.crawler.api.strategy.BaseCrawlerStrategy}
     *                    will be used
     * @return this step for chaining
     */
    public PollSourceStep<R, T, O> errorRouter(ErrorRouter errorRouter) {
        this.ports = getPorts(ports.bus(), ports.inboxWriter(), errorRouter);
        return this;
    }

    private CrawlerPorts getPorts(EventBusEmitter bus, InboxWriter inboxWriter, ErrorRouter errorRouter) {
        return CrawlerPorts.builder()
                .bus(SafeEventBusEmitterEmitter.of(bus))
                .inbox(SafeInboxWriter.of(inboxWriter))
                .errorRouter(errorRouter)
                .build();
    }

    /**
     * Builds and returns the configured {@link CrawlerStrategy}.
     *
     * @return a fully configured {@link CrawlerStrategy} ready to be executed
     */
    public CrawlerStrategy create() {
        return new PollCrawlerStrategy<>(source, transformer, ports, getSenderOrDefault());
    }

    private String getSenderOrDefault() {
        return Optional.ofNullable(sender)
                .orElseGet(() -> "PollCrawler" + "-" + UUID.randomUUID().toString().substring(0, 8));
    }

    /**
     * Applies the given {@link ProcessorFormat} to this step, returning a new
     * step with updated type parameters matching the format's output types.
     *
     * <p>
     * The transformer produced by the format is wrapped in the corresponding
     * {@code Safe*} decorators automatically.
     * </p>
     *
     * @param <NT>   the new intermediate type produced by the format's deserializer
     * @param <NO>   the new record type produced by the format's splitter
     * @param format the processing format to apply; must not be {@code null}
     * @return a new {@link PollSourceStep} with the format applied
     */
    public <NT, NO> PollSourceStep<R, NT, NO> withFormat(ProcessorFormat<R, NT, NO> format) {
        Transformer<R, NT, NO> pipeline = format.pipeline();
        return new PollSourceStep<R, NT, NO>(source, ports)
                .senderName(sender)
                .transformer(pipeline);
    }
}
