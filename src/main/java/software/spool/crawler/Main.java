package software.spool.crawler;

import software.spool.core.adapter.memory.InMemoryEventBroker;
import software.spool.core.adapter.otel.OTELConfig;
import software.spool.core.model.spool.SpoolNode;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.port.serde.NamingConvention;
import software.spool.core.utils.polling.PollingConfiguration;
import software.spool.crawler.api.Crawler;
import software.spool.crawler.api.builder.CrawlerBuilderFactory;
import software.spool.crawler.api.builder.EventMappingSpecification;
import software.spool.crawler.api.utils.CrawlerErrorRouter;
import software.spool.crawler.api.utils.CrawlerPorts;
import software.spool.crawler.api.utils.StandardNormalizer;
import software.spool.crawler.internal.adapter.http.HTTPPollSource;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        OTELConfig.init("crawler");
        InMemoryEventBroker broker = new InMemoryEventBroker();

        Crawler with = CrawlerBuilderFactory.poll(new HTTPPollSource("https://data.sec.gov/api/xbrl/frames/us-gaap/Assets/USD/CY2023Q4I.json", "test"))
                .schedule(PollingConfiguration.every(Duration.ofSeconds(10)))
                .ports(CrawlerPorts.builder()
                        .inbox(e -> IdempotencyKey.of("sec", e.payload()))
                        .bus(broker).build())
                .enrichRules(List.of())
                .eventMapping(new EventMappingSpecification(NamingConvention.SNAKE_CASE))
                .withErrorRouter(CrawlerErrorRouter.defaults(broker))
                .createWith(StandardNormalizer.JSON_OBJECT);
        SpoolNode.create().register(with).start();
    }
}
