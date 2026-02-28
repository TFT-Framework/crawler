# spool-crawler

A lightweight, extensible crawler framework for event-driven systems.  
It handles the full polling pipeline — fetch → deserialize → split → serialize → persist — while publishing lifecycle events to a bus and routing errors uniformly.

---

## Table of Contents

- [Installation](#installation)
- [Core Concepts](#core-concepts)
- [Package Structure](#package-structure)
- [Quick Start](#quick-start)
- [Examples](#examples)
  - [Minimal: HTTP JSON array](#minimal-http-json-array)
  - [Minimal: SQL ResultSet](#minimal-sql-resultset)
  - [Custom format pipeline](#custom-format-pipeline)
  - [Custom InboxWriter and EventBus](#custom-inboxwriter-and-eventbus)
  - [Custom error routing](#custom-error-routing)

---

## Installation

```xml
<dependency>
    <groupId>software.spool</groupId>
    <artifactId>crawler</artifactId>
    <version>1.3.1-SNAPSHOT</version>
</dependency>
```

---

## Core Concepts

| Concept | What you do |
|---|---|
| `PollSource<R>` | Implement to fetch raw data (HTTP, DB, file…) |
| `ProcessorFormat<R,P,T>` | Describes how to deserialize, split and serialize records |
| `InboxWriter` | Implement to persist each record (DB, queue, in-memory…) |
| `EventBus` | Implement to receive lifecycle events (`RawDataWrittenToInbox`, `SourceFailed`…) |
| `ErrorRouter` | Optional DSL to map exception types to handlers |
| `CrawlerPorts` | Aggregates `InboxWriter`, `EventBus` and `ErrorRouter` |
| `Crawlers` | DSL entry point — composes everything and returns a `CrawlerStrategy` |

The flow for a poll-based crawler is:

```
PollSource.poll()
  → deserialize  (SourceDeserializer)
  → split        (SourceSplitter)
  → serialize    (SourceSerializer)
  → InboxWriter.receive()
  → EventBus.emit(RawDataWrittenToInbox)
```

Every step is wrapped in safe error handling; failures are dispatched through the `ErrorRouter`.

---

## Package Structure

```
software.spool.crawler
├── api                          public API — everything a user needs to import
│   ├── dsl
│   │   ├── Crawlers             entry point (factory methods)
│   │   └── PollSourceStep       fluent builder returned by Crawlers.poll()
│   ├── exception                typed exceptions (SpoolException hierarchy)
│   ├── port                     interfaces you implement
│   │   ├── EventBus
│   │   ├── InboxWriter
│   │   └── InboxEntryId
│   ├── source                   source interfaces you implement
│   │   ├── PollSource<R>
│   │   ├── StreamSource<R>
│   │   └── WebhookSource
│   ├── strategy
│   │   ├── CrawlerStrategy      execute() contract
│   │   └── BaseCrawlerStrategy  optional base class for custom strategies
│   ├── ErrorRouter
│   ├── Formats                  built-in format presets
│   └── ProcessorFormat<R,P,T>   interface for custom pipelines
└── internal                     implementation details — do not depend on these
    ├── port                     internal source decomposition interfaces
    ├── strategy
    │   └── PollCrawlerStrategy  default poll execution logic
    └── utils
        ├── CrawlerPorts         bundles ports for injection
        ├── InMemoryInboxWriter  for tests
        ├── JdbcInboxWriter      PostgreSQL implementation
        └── factory              deserializer / splitter / serializer factories
```

---

## Quick Start

```java
// 1. Implement PollSource
class MyApiSource implements PollSource<String> {
    public String poll() {
        return httpClient.get("https://api.example.com/records");
    }
    public String sourceId() { return "my-api"; }
}

// 2. Wire and run
CrawlerStrategy crawler = Crawlers.poll(new MyApiSource())
        .withFormat(Formats.JSON_ARRAY)   // parse JSON array, emit one event per element
        .senderName("my-api")
        .create();

crawler.execute();
```

---

## Examples

### Minimal: HTTP JSON array

The source returns a JSON array string. `Formats.JSON_ARRAY` parses it, splits it into individual nodes, and serializes each one as a JSON string into the inbox.

```java
public class GeminiTradeCrawlerSource implements PollSource<String> {
    private final HttpClient http = HttpClient.newHttpClient();

    @Override
    public String poll() throws SpoolException {
        try {
            var req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.gemini.com/v1/trades/btcusd?limit_trades=10"))
                    .GET().build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new SourcePollException("Gemini request failed", e);
        }
    }

    @Override
    public String sourceId() { return "gemini-btcusd"; }
}

// Wire it — uses InMemoryInboxWriter and a logger bus by default
CrawlerStrategy crawler = Crawlers.poll(new GeminiTradeCrawlerSource())
        .withFormat(Formats.JSON_ARRAY)
        .senderName("GeminiHTTP")
        .create();

crawler.execute();
```

---

### Minimal: SQL ResultSet

```java
public class OrdersSource implements PollSource<ResultSet> {
    private final Connection conn;
    public OrdersSource(Connection conn) { this.conn = conn; }

    @Override
    public ResultSet poll() throws SpoolException {
        try {
            return conn.createStatement()
                       .executeQuery("SELECT * FROM orders WHERE status = 'NEW'");
        } catch (SQLException e) {
            throw new SourcePollException("Failed to query orders", e);
        }
    }

    @Override
    public String sourceId() { return "orders-db"; }
}

CrawlerStrategy crawler = Crawlers.poll(new OrdersSource(connection))
        .withFormat(Formats.RESULT_SET)   // maps each row to Map<String,Object> → JSON
        .senderName("orders-db")
        .create();

crawler.execute();
```

---

### Custom format pipeline

Use `ProcessorFormat` when the built-in formats don't fit. Here, a CSV source is deserialized line by line into domain records and serialized to JSON.

```java
ProcessorFormat<String, List<Map<String, String>>, Map<String, String>> csvFormat =
    () -> TransformerFactory.of(
        DeserializerFactory.csv(),        // String → List<Map<String,String>>
        SplitterFactory.single(),         // keep as-is per element
        SerializerFactory.map()           // Map<String,String> → JSON string
    );

public class CsvFileSource implements PollSource<String> {
    @Override
    public String poll() throws SpoolException {
        try {
            return Files.readString(Path.of("/data/orders.csv"));
        } catch (IOException e) {
            throw new SourcePollException("Cannot read CSV file", e);
        }
    }
    @Override
    public String sourceId() { return "csv-file"; }
}

CrawlerStrategy crawler = Crawlers.poll(new CsvFileSource())
        .withFormat(csvFormat)
        .senderName("csv-file")
        .create();

crawler.execute();
```

---

### Custom InboxWriter and EventBus

Supply your own infrastructure adapters via `CrawlerPorts`:

```java
// Kafka EventBus
EventBus kafkaBus = event ->
    kafkaProducer.send(new ProducerRecord<>("spool-events", event.eventId(),
        objectMapper.writeValueAsString(event)));

// Custom InboxWriter backed by your JPA repository
InboxWriter jpaWriter = rawData -> {
    InboxEvent entity = new InboxEvent(rawData.sender(), rawData.payload());
    return new InboxEntryId(String.valueOf(repository.save(entity).getId()));
};

CrawlerPorts ports = CrawlerPorts.builder()
        .bus(kafkaBus)
        .inbox(jpaWriter)
        .build();

CrawlerStrategy crawler = Crawlers.poll(new GeminiTradeCrawlerSource())
        .withFormat(Formats.JSON_ARRAY)
        .ports(ports)
        .senderName("GeminiHTTP")
        .create();

crawler.execute();
```

---

### Custom error routing

`ErrorRouter` maps exception types to handlers, with an optional fallback:

```java
ErrorRouter errorRouter = new ErrorRouter()
        .on(SourcePollException.class, e -> {
            log.error("Source unreachable: {}", e.getMessage());
            alertService.send("CRAWLER_DOWN", e.getMessage());
        })
        .on(InboxWriteException.class, e -> {
            log.error("Inbox write failed: {}", e.getMessage());
            deadLetterQueue.enqueue(e.getMessage());
        })
        .on(DeserializationException.class, e -> {
            log.warn("Bad payload discarded: {}", e.rawPayload());
        })
        .orElse(e -> log.error("Unhandled crawler error", e));

CrawlerPorts ports = CrawlerPorts.builder()
        .bus(myBus)
        .inbox(myInboxWriter)
        .errorRouter(errorRouter)
        .build();

CrawlerStrategy crawler = Crawlers.poll(new GeminiTradeCrawlerSource())
        .withFormat(Formats.JSON_ARRAY)
        .ports(ports)
        .senderName("GeminiHTTP")
        .create();

crawler.execute();
```

---

## Built-in Formats

| Constant | Input type | Description |
|---|---|---|
| `Formats.JSON_ARRAY` | `String` (JSON array) | Parse → split each element → serialize to JSON |
| `Formats.YAML_ARRAY` | `String` (YAML array) | Same as above, YAML input |
| `Formats.RESULT_SET` | `ResultSet` | Map each row to `Map<String,Object>` → serialize to JSON |

---

## Lifecycle Events

Every execution emits events to the `EventBus`:

| Event | Fired when |
|---|---|
| `RawDataReadFromSource` | A record is read from the source |
| `RawDataWrittenToInbox` | A record is successfully written to the inbox |
| `SourceFailed` | The source could not be opened or polled |
| `InboxFailed` | A write to the inbox failed |

All events implement `SpoolEvent` and carry an `eventId`, `eventType`, `timestamp`, and `sender`.

---

## License

Apache License 2.0 — see [LICENSE](http://www.apache.org/licenses/LICENSE-2.0.txt).
