package software.spool.crawler.api.dsl;

import software.spool.crawler.api.ProcessorFormat;
import software.spool.crawler.api.source.PollSource;
import software.spool.crawler.api.strategy.CrawlerStrategy;
import software.spool.crawler.internal.strategy.PollCrawlerStrategy;
import software.spool.crawler.internal.utils.CrawlerPorts;
import software.spool.crawler.internal.utils.factory.Transformer;

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

    public CrawlerStrategy create() {
        return new PollCrawlerStrategy<>(source, transformer, ports, sender);
    }

    public <NT, NO> PollSourceStep<R, NT, NO> withFormat(ProcessorFormat<R, NT, NO> format) {
        Transformer<R, NT, NO> pipeline = format.pipeline();
        return new PollSourceStep<R, NT, NO>(source, ports)
                .senderName(sender)
                .transformer(pipeline);
    }
}
