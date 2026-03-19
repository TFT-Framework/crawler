package software.spool.crawler.example.filesystem.application;

import software.spool.core.adapter.ConsoleEventTracer;
import software.spool.core.adapter.InMemoryEventBus;
import software.spool.core.port.EventBus;
import software.spool.core.port.decorator.TraceEventBus;
import software.spool.core.utils.ErrorRouter;
import software.spool.crawler.api.Crawler;
import software.spool.crawler.api.builder.CrawlerBuilderFactory;
import software.spool.crawler.api.port.InboxWriter;
import software.spool.crawler.api.utils.CrawlerErrorRouter;
import software.spool.crawler.api.utils.CrawlerPorts;
import software.spool.crawler.api.utils.Formats;
import software.spool.crawler.example.filesystem.application.io.FileOrderReader;
import software.spool.crawler.example.filesystem.domain.model.OrderReceived;
import software.spool.crawler.example.filesystem.domain.model.dto.OrderDTO;
import software.spool.crawler.api.adapter.InMemoryInboxWriter;

public class Application {
    private final Crawler crawler;
    private final EventBus bus;
    private final InboxWriter inbox;
    private final ErrorRouter router;

    public Application() {
        bus = TraceEventBus.of(new InMemoryEventBus()).with(new ConsoleEventTracer());
        inbox = new InMemoryInboxWriter();
        router = CrawlerErrorRouter.defaults(bus);
        crawler = initializeCrawler();
    }

    private Crawler initializeCrawler() {
        return CrawlerBuilderFactory.poll(new FileOrderReader())
                .ports(CrawlerPorts.builder()
                        .bus(bus)
                        .inbox(inbox)
                        .errorRouter(router).build())
                .withFormat(Formats.JSON_ARRAY)
                .withDomainEvent(OrderDTO.class, (dto, idempotencyKey) -> OrderReceived.from(dto))
                .withDomainEvent(OrderReceived.class)
                .create();
    }

    public void run() {
        crawler.startCrawling();
    }
}
