package software.spool.crawler.api.dsl;

import software.spool.crawler.api.ErrorRouter;
import software.spool.crawler.api.ProcessorFormat;
import software.spool.crawler.api.port.EventBus;
import software.spool.crawler.api.port.InboxWriter;
import software.spool.crawler.api.source.PollSource;
import software.spool.crawler.api.strategy.CrawlerStrategy;
import software.spool.crawler.internal.strategy.PollCrawlerStrategy;
import software.spool.crawler.internal.utils.CrawlerPorts;
import software.spool.crawler.internal.utils.factory.Transformer;

import java.util.Optional;
import java.util.UUID;

public class PollSourceStep<R, T, O> {
    private final PollSource<R> source;
    private Transformer<R, T, O> transformer;
    private CrawlerPorts ports;
    private String sender;

    public PollSourceStep(PollSource<R> source, CrawlerPorts ports) {
        this.source = source;
        this.ports = ports;
    }

    public PollSourceStep<R, T, O> transformer(Transformer<R, T, O> transformer) {
        this.transformer = transformer;
        return this;
    }

    public PollSourceStep<R, T, O> ports(CrawlerPorts ports) {
        this.ports = ports;
        return this;
    }

    public PollSourceStep<R, T, O> senderName(String sender) {
        this.sender = sender;
        return this;
    }

    public PollSourceStep<R, T, O> bus(EventBus bus) {
        this.ports = getPorts(bus, ports.inboxWriter(), ports.errorRouter());
        return this;
    }

    public PollSourceStep<R, T, O> inbox(InboxWriter inbox) {
        this.ports = getPorts(ports.bus(), inbox, ports.errorRouter());
        return this;
    }

    public PollSourceStep<R, T, O> errorRouter(ErrorRouter errorRouter) {
        this.ports = getPorts(ports.bus(), ports.inboxWriter(), errorRouter);
        return this;
    }

    private CrawlerPorts getPorts(EventBus bus, InboxWriter inboxWriter, ErrorRouter errorRouter) {
        return CrawlerPorts.builder()
                .bus(bus)
                .inbox(inboxWriter)
                .errorRouter(errorRouter)
                .build();
    }

    public CrawlerStrategy create() {
        return new PollCrawlerStrategy<>(source, transformer, ports, getSenderOrDefault());
    }

    private String getSenderOrDefault() {
        return Optional.ofNullable(sender).orElseGet(() ->
                "PollCrawler" + "-" + UUID.randomUUID().toString().substring(0, 8));
    }

    public <NT, NO> PollSourceStep<R, NT, NO> withFormat(ProcessorFormat<R, NT, NO> format) {
        Transformer<R, NT, NO> pipeline = format.pipeline();
        return new PollSourceStep<R, NT, NO>(source, ports)
                .senderName(sender)
                .transformer(pipeline);
    }
}
