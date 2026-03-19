package software.spool.crawler.api.builder;

import software.spool.core.exception.SpoolException;
import software.spool.core.model.Event;
import software.spool.core.model.IdempotencyKey;
import software.spool.core.port.PayloadDeserializer;
import software.spool.core.utils.DomainEventMapping;
import software.spool.core.utils.NamingConvention;
import software.spool.crawler.api.Crawler;
import software.spool.crawler.api.utils.TransformerFormat;
import software.spool.crawler.api.port.source.PollSource;
import software.spool.crawler.api.strategy.CrawlerStrategy;
import software.spool.crawler.internal.control.ItemCapturedHandler;
import software.spool.crawler.internal.port.decorator.SafePayloadDeserializer;
import software.spool.crawler.internal.port.decorator.SafePayloadSplitter;
import software.spool.crawler.internal.port.decorator.SafePollSource;
import software.spool.crawler.internal.port.decorator.SafeRecordSerializer;
import software.spool.crawler.internal.strategy.PollCrawlerStrategy;
import software.spool.crawler.api.utils.CrawlerPorts;
import software.spool.crawler.internal.utils.DomainEventEmitter;
import software.spool.crawler.internal.utils.TypedDomainMapping;
import software.spool.crawler.internal.utils.factory.Transformer;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Fluent builder that configures a poll-based {@link CrawlerStrategy}.
 *
 * <p>
 * Instances are normally obtained via
 * {@link CrawlerBuilderFactory#poll(PollSource)}
 * and then further customised with the chainable setter methods before calling
 * {@link #create()} to build the strategy.
 * </p>
 *
 * <p>
 * All ports passed to the setter methods are automatically wrapped in their
 * corresponding {@code Safe*} decorators to normalise unchecked exceptions into
 * typed {@link SpoolException} subclasses.
 * </p>
 *
 * @param <I> the raw type returned by the configured {@link PollSource}
 * @param <T> the intermediate type after deserialization
 * @param <O> the individual record type produced by the splitter
 */
public class PollSourceBuilder<I, T, O> {
    private final PollSource<I> source;
    private Transformer<T, O> transformer;
    private CrawlerPorts ports;
    private NamingConvention namingConvention;
    private final List<TypedDomainMapping> domainMappings;
    private final List<String> defaultPartitionAttributes;

    /**
     * Creates a new step wrapping the given source and ports.
     *
     * <p>
     * The source is immediately decorated with {@link SafePollSource} to
     * normalise any unchecked exceptions thrown during polling.
     * </p>
     *
     * @param source the poll source; must not be {@code null}
     */
    public PollSourceBuilder(PollSource<I> source) {
        this(SafePollSource.of(source), null, NamingConvention.SNAKE_CASE, new ArrayList<>(), new ArrayList<>());
    }

    private PollSourceBuilder(PollSource<I> source, CrawlerPorts ports, NamingConvention namingConvention,
            List<TypedDomainMapping> domainMappings, List<String> defaultPartitionAttributes) {
        this.source = SafePollSource.of(source);
        this.ports = ports;
        this.namingConvention = namingConvention;
        this.domainMappings = domainMappings;
        this.defaultPartitionAttributes = defaultPartitionAttributes;
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
    public PollSourceBuilder<I, T, O> transformer(Transformer<T, O> transformer) {
        this.transformer = Transformer.of(
                SafePayloadDeserializer.of(transformer.deserializer()),
                SafePayloadSplitter.of(transformer.splitter()),
                SafeRecordSerializer.of(transformer.serializer()));
        return this;
    }

    /**
     * Replaces all ports with the given {@link CrawlerPorts} bundle.
     *
     * @param ports the new ports; must not be {@code null}
     * @return this step for chaining
     */
    public PollSourceBuilder<I, T, O> ports(CrawlerPorts ports) {
        this.ports = ports;
        return this;
    }

    /**
     * Applies the given {@link TransformerFormat} to this step, returning a new
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
     * @return a new {@link PollSourceBuilder} with the format applied
     */
    public <NT, NO> PollSourceBuilder<I, NT, NO> withFormat(TransformerFormat<NT, NO> format) {
        Transformer<NT, NO> pipeline = format.pipeline();
        return new PollSourceBuilder<I, NT, NO>(source, ports, namingConvention, domainMappings, defaultPartitionAttributes)
                .transformer(pipeline);
    }

    /**
     * Sets the naming convention used when deserializing domain events.
     *
     * <p>
     * The naming convention controls how JSON field names are mapped to Java
     * record/class fields. Defaults to {@link NamingConvention#SNAKE_CASE}.
     * </p>
     *
     * @param namingConvention the naming convention to use; must not be
     *                         {@code null}
     * @return this step for chaining
     */
    public PollSourceBuilder<I, T, O> withNamingConvention(NamingConvention namingConvention) {
        this.namingConvention = namingConvention;
        return this;
    }

    private <D> PayloadDeserializer<D> deserializerFor(Class<D> type) {
        return namingConvention.deserializerFor(type);
    }

    public PollSourceBuilder<I, T, O> withDomainEvent(Class<? extends Event> eventType, String... partitionAttributes) {
        domainMappings.add(new TypedDomainMapping(eventType,
                DomainEventMapping.of(deserializerFor(eventType)),
                List.of(partitionAttributes)));
        return this;
    }

    public <D> PollSourceBuilder<I, T, O> withDomainEvent(Class<D> dtoType, BiFunction<D,
            IdempotencyKey, Event> toEvent, String... partitionAttributes) {
        domainMappings.add(new TypedDomainMapping(dtoType,
                DomainEventMapping.of(deserializerFor(dtoType), toEvent),
                List.of(partitionAttributes)));
        return this;
    }

    public PollSourceBuilder<I, T, O> withPartitionAttributes(String... attributes) {
        defaultPartitionAttributes.addAll(List.of(attributes));
        return this;
    }

    /**
     * Builds and returns the configured {@link CrawlerStrategy}.
     *
     * @return a fully configured {@link CrawlerStrategy} ready to be executed
     */
    public Crawler create() {
        return new Crawler(new PollCrawlerStrategy<>(source, transformer, ports.errorRouter(), createHandler()), ports.errorRouter());
    }

    private ItemCapturedHandler createHandler() {
        if (!domainMappings.isEmpty() && !defaultPartitionAttributes.isEmpty())
            throw new IllegalArgumentException("Only one can be used at the same time. Please, use withDomainEvent(...) or withPartitionAttributes(...) but not both.");
        return new ItemCapturedHandler(source.sourceId(), ports,
                new DomainEventEmitter(ports.bus(), domainMappings), defaultPartitionAttributes);
    }
}
