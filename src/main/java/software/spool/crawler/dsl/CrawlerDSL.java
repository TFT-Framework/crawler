package software.spool.crawler.dsl;

import software.spool.core.adapter.console.ConsoleTracedEventBus;
import software.spool.core.adapter.jackson.PayloadDeserializerFactory;
import software.spool.core.port.bus.EventBusEmitter;
import software.spool.core.port.decorator.TraceEventBus;
import software.spool.core.port.serde.NamingConvention;
import software.spool.core.utils.polling.PollingConfiguration;
import software.spool.crawler.Main;
import software.spool.crawler.api.Crawler;
import software.spool.crawler.api.adapter.InMemoryInboxWriter;
import software.spool.crawler.api.builder.CrawlerBuilderFactory;
import software.spool.crawler.api.builder.EventMappingSpecification;
import software.spool.crawler.api.port.InboxWriter;
import software.spool.crawler.api.utils.CrawlerErrorRouter;
import software.spool.crawler.api.utils.CrawlerPorts;
import software.spool.crawler.api.utils.Formats;
import software.spool.crawler.internal.adapter.http.HTTPPollSource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CrawlerDSL {

    private final Map<String, Object> root;

    private CrawlerDSL(Map<String, Object> root) {
        this.root = root;
    }

    // ─── Entry point ──────────────────────────────────────────────────────────

    public static CrawlerDSL fromDescriptor(String classpathPath) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(
                Objects.requireNonNull(Main.class.getResourceAsStream(classpathPath)));
        String yaml = new String(bis.readAllBytes());
        Map<String, Object> root = PayloadDeserializerFactory.yaml()
                .convention(NamingConvention.KEBAB_CASE).asMap().deserialize(yaml);
        return new CrawlerDSL(root);
    }

    // ─── Build all crawlers ───────────────────────────────────────────────────

    public List<Crawler> buildAll() {
        Map<String, Object> spool = section(root, "spool");
        Map<String, Object> infra = section(spool, "infrastructure");

        EventBusEmitter bus   = buildEventBus(section(infra, "event-bus"));
        InboxWriter     inbox = buildInbox(section(infra, "inbox"));

        return CrawlerDSL.<Map<String, Object>>list(spool, "crawlers").stream()
                .map(def -> buildCrawler(def, bus, inbox))
                .toList();
    }

    // ─── Infrastructure ───────────────────────────────────────────────────────

    private EventBusEmitter buildEventBus(Map<String, Object> def) {
        if (def.containsKey("kafka")) {
            String url = (String) section(def, "kafka").get("url");
            return EventBusFactory.kafkaEmitter(url);              // adapta a tu API
        }
        if (def.containsKey("in-memory")) {
            return TraceEventBus.of(EventBusFactory.console()).with(new ConsoleTracedEventBus());
        }
        throw new IllegalArgumentException("Unknown event-bus type: " + def.keySet());
    }

    private InboxWriter buildInbox(Map<String, Object> def) {
        if (def.containsKey("sql")) {
            Map<String, Object> sql = section(def, "sql");
            return InboxWriterFactory.sql(                  // adapta a tu API
                    (String) sql.get("database"),
                    (String) sql.get("user"),
                    (String) sql.get("password"));
        }
        if (def.containsKey("in-memory")) {
            return new InMemoryInboxWriter();
        }
        throw new IllegalArgumentException("Unknown inbox type: " + def.keySet());
    }

    // ─── Crawler ──────────────────────────────────────────────────────────────

    private Crawler buildCrawler(Map<String, Object> def, EventBusEmitter bus, InboxWriter inbox) {
        if (def.containsKey("poll")) {
            return buildPollCrawler(section(def, "poll"), bus, inbox);
        }
        throw new IllegalArgumentException("Unknown crawler type in: " + def.keySet());
    }

    private Crawler buildPollCrawler(Map<String, Object> poll, EventBusEmitter bus, InboxWriter inbox) {
        return CrawlerBuilderFactory.poll(buildPollSource(section(poll, "source")))
                .ports(CrawlerPorts.builder()
                        .bus(bus)
                        .inbox(inbox)
                        .build())
                .schedule(buildSchedule(section(poll, "schedule")))
                .withErrorRouter(CrawlerErrorRouter.defaults(bus))
                .eventMapping(buildEventMapping(section(poll, "event-mapping")))
                .createWith(Formats.valueOf((String) poll.get("format")));
    }

    // ─── Source ───────────────────────────────────────────────────────────────

    private HTTPPollSource buildPollSource(Map<String, Object> def) {
        String sourceId = (String) def.get("id");
        if (def.containsKey("http")) {
            Map<String, Object> http = section(def, "http");
            return new HTTPPollSource((String) http.get("url"), sourceId);
        }
        // if (def.containsKey("jdbc")) { ... }
        throw new IllegalArgumentException("Unknown source type in: " + def.keySet());
    }

    // ─── Schedule ─────────────────────────────────────────────────────────────

    private PollingConfiguration buildSchedule(Map<String, Object> def) {
        Object every = def.get("every");
        Duration duration;
        if (every instanceof Integer i) {
            duration = Duration.ofSeconds(i);
        } else if (every instanceof String s) {
            duration = parseDuration(s);
        } else {
            throw new IllegalArgumentException("Invalid schedule value: " + every);
        }
        return PollingConfiguration.every(duration);
    }

    private Duration parseDuration(String value) {
        if (value.endsWith("s")) return Duration.ofSeconds(Long.parseLong(value.replace("s", "")));
        if (value.endsWith("m")) return Duration.ofMinutes(Long.parseLong(value.replace("m", "")));
        if (value.endsWith("h")) return Duration.ofHours(Long.parseLong(value.replace("h", "")));
        return Duration.parse(value); // ISO-8601 fallback: PT5S
    }

    // ─── Event mapping ────────────────────────────────────────────────────────

    private EventMappingSpecification buildEventMapping(Map<String, Object> def) {
        NamingConvention convention = NamingConvention.valueOf((String) def.get("naming-convention"));
        EventMappingSpecification spec = new EventMappingSpecification(convention);
        CrawlerDSL.<String>list(def, "partition-attributes").forEach(spec::addPartitionAttributes);
        return spec;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private static Map<String, Object> section(Map<String, Object> map, String key) {
        return (Map<String, Object>) map.get(key);
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> list(Map<String, Object> map, String key) {
        return (List<T>) map.get(key);
    }
}