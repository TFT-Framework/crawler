package software.spool.crawler.api.builder;

import software.spool.core.model.Event;
import software.spool.core.model.IdempotencyKey;
import software.spool.core.port.PayloadDeserializer;
import software.spool.crawler.api.utils.DomainEventMapping;
import software.spool.crawler.api.utils.ProcessorFormat;
import software.spool.crawler.api.port.source.PollSource;
import software.spool.crawler.api.strategy.CrawlerStrategy;
import software.spool.crawler.internal.decorator.*;
import software.spool.crawler.internal.strategy.PollCrawlerStrategy;
import software.spool.crawler.api.utils.CrawlerPorts;
import software.spool.crawler.internal.utils.factory.DomainMapperFactory;
import software.spool.crawler.internal.utils.factory.Transformer;
import software.spool.crawler.api.utils.NamingConvention;

import java.util.ArrayList;
import java.util.List;
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
 * typed {@link software.spool.core.exception.SpoolException} subclasses.
 * </p>
 *
 * @param <I> the raw type returned by the configured {@link PollSource}
 * @param <T> the intermediate type after deserialization
 * @param <O> the individual record type produced by the splitter
 */
public class PollSourceBuilder<I, T, O> {
    private final PollSource<I> source;
    private Transformer<I, T, O> transformer;
    private CrawlerPorts ports;
    private NamingConvention namingConvention;
    private List<DomainEventMapping<?>> domainMappings;

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
        this(SafePollSource.of(source), null, new ArrayList<>());
    }

    private PollSourceBuilder(PollSource<I> source, CrawlerPorts ports, List<DomainEventMapping<?>> domainMappings) {
        this.source = SafePollSource.of(source);
        this.ports = ports;
        this.domainMappings = domainMappings;
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
    public PollSourceBuilder<I, T, O> transformer(Transformer<I, T, O> transformer) {
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
     * Builds and returns the configured {@link CrawlerStrategy}.
     *
     * @return a fully configured {@link CrawlerStrategy} ready to be executed
     */
    public CrawlerStrategy create() {
        return new PollCrawlerStrategy<>(source, transformer, ports, domainMappings);
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
     * @return a new {@link PollSourceBuilder} with the format applied
     */
    public <NT, NO> PollSourceBuilder<I, NT, NO> withFormat(ProcessorFormat<I, NT, NO> format) {
        Transformer<I, NT, NO> pipeline = format.pipeline();
        return new PollSourceBuilder<I, NT, NO>(source, ports, domainMappings)
                .transformer(pipeline);
    }

    public PollSourceBuilder<I, T, O> withNamingConvention(NamingConvention namingConvention) {
        this.namingConvention = namingConvention;
        return this;
    }

    private <D> PayloadDeserializer<String, D> deserializerFor(Class<D> type) {
        return switch (namingConvention) {
            case CAMEL_CASE  -> DomainMapperFactory.camelCase(type);
            case SNAKE_CASE  -> DomainMapperFactory.snakeCase(type);
            case PASCAL_CASE -> DomainMapperFactory.pascalCase(type);
            case KEBAB_CASE  -> DomainMapperFactory.kebabCase(type);
        };
    }

    public PollSourceBuilder<I, T, O> withDomainEvent(Class<? extends Event> eventType) {
        domainMappings.add(DomainEventMapping.of(deserializerFor(eventType)));
        return this;
    }

    public <D> PollSourceBuilder<I, T, O> withDomainEvent(Class<D> dtoType, BiFunction<D, IdempotencyKey, Event> toEvent) {
        domainMappings.add(DomainEventMapping.of(deserializerFor(dtoType), toEvent));
        return this;
    }
}
