# Spool — Crawler

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/build-Maven-C71A36.svg)](https://maven.apache.org/)

**Crawler** is the **Spool** framework module responsible for **fetching raw data from external sources and delivering it to the inbox**. It handles the full polling pipeline — fetch → deserialize → split → serialize → persist — while publishing lifecycle events to a bus and routing errors uniformly.

---

## Table of Contents

- [Architecture](#architecture)
- [Key Concepts](#key-concepts)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Examples](#examples)
  - [HTTP JSON Array](#http-json-array)
  - [YAML Source](#yaml-source)
  - [SQL ResultSet](#sql-resultset)
  - [Custom Transformer Pipeline](#custom-transformer-pipeline)
  - [Domain Event Mapping](#domain-event-mapping)
  - [Custom Ports (InboxWriter + EventBus)](#custom-ports-inboxwriter--eventbus)
  - [Custom Error Routing](#custom-error-routing)
- [API Reference](#api-reference)
- [Built-in Formats](#built-in-formats)
- [Event Model](#event-model)
- [License](#license)

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                           Crawler                                │
│                                                                  │
│  PollSource.poll()                                               │
│       │                                                          │
│       ▼                                                          │
│  Deserializer ──► Splitter ──► Serializer                        │
│       │              │             │                              │
│       └──────────────┴─────────────┘                              │
│                      │                                           │
│                      ▼                                           │
│             InboxWriter.receive()                                │
│                      │                                           │
│            ┌─────────┴──────────┐                                │
│            ▼                    ▼                                 │
│     EventBus.emit(         EventBus.emit(                        │
│      SourceItemCaptured)    InboxItemStored)                     │
│                                                                  │
│  Errors ──► ErrorRouter ──► EventBus.emit(FailureEvent)          │
└──────────────────────────────────────────────────────────────────┘
```

The pipeline for a poll-based crawler:

1. **`PollSource.poll()`** — fetches raw data from the external source.
2. **Deserialize** — converts the raw output into a typed intermediate object.
3. **Split** — expands the intermediate object into individual records.
4. **Serialize** — converts each record into a `String` payload.
5. **`InboxWriter.receive()`** — persists each record with an idempotency key.
6. **EventBus** — emits `SourceItemCaptured` and `InboxItemStored` events.

Every step is wrapped in safe error handling; failures are dispatched through the `ErrorRouter`.

---

## Key Concepts

| Concept | Description |
|---|---|
| **`PollSource<R>`** | Interface you implement to fetch raw data (HTTP, DB, file…). |
| **`StreamSource<R>`** | Interface for push-based streaming sources (Kafka, WebSocket…). |
| **`WebhookSource`** | Interface for HTTP webhook endpoints. |
| **`TransformerFormat<T,O>`** | Describes a processing pipeline: deserializer + splitter + serializer. |
| **`Formats`** | Pre-built formats: `JSON_ARRAY`, `YAML_ARRAY`, `RESULT_SET`. |
| **`InboxWriter`** | Interface you implement to persist each record (DB, queue, in-memory…). |
| **`CrawlerPorts`** | Aggregates `InboxWriter`, `EventBusEmitter`, and `ErrorRouter`. |
| **`CrawlerBuilderFactory`** | DSL entry point — composes everything and returns a `CrawlerStrategy`. |
| **`ErrorRouter`** | Maps exception types to handlers. Default mapping provided by `CrawlerErrorRouter`. |

---

## Installation

### Maven (GitHub Packages)

```xml
<dependency>
    <groupId>io.github.spool-framework</groupId>
    <artifactId>crawler</artifactId>
    <version>1.0.2-SNAPSHOT</version>
</dependency>
```

Configure the GitHub Packages repository in your `pom.xml` or `settings.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/Spool-FRAMEWORK/*</url>
    </repository>
</repositories>
```

> Requires **Java 17+**.

---

## Quick Start

```java
// 1. Implement a PollSource that fetches a JSON array
public class MyApiSource implements PollSource<String> {
    @Override
    public String poll() {
        return httpClient.get("https://api.example.com/records");
    }

    @Override
    public String sourceId() {
        return "my-api";
    }
}

// 2. Build and execute the crawler
CrawlerStrategy crawler = CrawlerBuilderFactory.poll(new MyApiSource())
        .withFormat(Formats.JSON_ARRAY)
        .ports(CrawlerPorts.builder()
                .inbox(myInboxWriter)
                .bus(myEventBus)
                .build())
        .create();

crawler.execute();
```

---

## Examples

### HTTP JSON Array

The most common use case: fetching a JSON array from a REST API and storing each element in the inbox.

```java
public class GeminiTradeSource implements PollSource<String> {
    private final HttpClient http = HttpClient.newHttpClient();

    @Override
    public String poll() throws SpoolException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.gemini.com/v1/trades/btcusd?limit_trades=10"))
                    .GET()
                    .build();
            return http.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new SourcePollException(sourceId(), e.getMessage(), e);
        }
    }

    @Override
    public String sourceId() {
        return "gemini-btcusd";
    }
}

// Uses InMemoryInboxWriter for local testing
CrawlerStrategy crawler = CrawlerBuilderFactory.poll(new GeminiTradeSource())
        .withFormat(Formats.JSON_ARRAY)
        .ports(CrawlerPorts.builder()
                .inbox(new InMemoryInboxWriter())
                .bus(new InMemoryEventBus())
                .build())
        .create();

crawler.execute();
```

---

### YAML Source

Reading a YAML sequence from a file and splitting each element into individual records.

```java
public class YamlFileSource implements PollSource<String> {
    @Override
    public String poll() throws SpoolException {
        try {
            return Files.readString(Path.of("config/items.yaml"));
        } catch (IOException e) {
            throw new SourcePollException(sourceId(), e.getMessage(), e);
        }
    }

    @Override
    public String sourceId() {
        return "yaml-file";
    }
}

CrawlerStrategy crawler = CrawlerBuilderFactory.poll(new YamlFileSource())
        .withFormat(Formats.YAML_ARRAY)
        .ports(CrawlerPorts.builder()
                .inbox(myInboxWriter)
                .bus(myEventBus)
                .build())
        .create();

crawler.execute();
```

---

### SQL ResultSet

Crawling rows from a JDBC query. Each row is projected into a `Map<String, Object>` and serialized to JSON.

```java
public class OrderSource implements PollSource<ResultSet> {
    private Connection connection;

    @Override
    public PollSource<ResultSet> open() {
        connection = DriverManager.getConnection("jdbc:postgresql://localhost/mydb", "user", "pass");
        return this;
    }

    @Override
    public ResultSet poll() throws SpoolException {
        try {
            return connection.createStatement()
                    .executeQuery("SELECT id, name, total FROM orders WHERE status = 'NEW'");
        } catch (SQLException e) {
            throw new SourcePollException(sourceId(), e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        try { if (connection != null) connection.close(); } catch (SQLException ignored) {}
    }

    @Override
    public String sourceId() {
        return "orders-db";
    }
}

CrawlerStrategy crawler = CrawlerBuilderFactory.poll(new OrderSource())
        .withFormat(Formats.RESULT_SET)
        .ports(CrawlerPorts.builder()
                .inbox(myInboxWriter)
                .bus(myEventBus)
                .build())
        .create();

crawler.execute();
```

---

### Custom Transformer Pipeline

When the built-in formats don't fit, create your own `TransformerFormat` with custom deserialization, splitting, and serialization logic.

```java
TransformerFormat<List<MyRecord>, MyRecord> customFormat = () -> Transformer.of(
        payload -> objectMapper.readValue(payload, new TypeReference<List<MyRecord>>() {}),
        list    -> list.stream(),
        record  -> objectMapper.writeValueAsString(record)
);

CrawlerStrategy crawler = CrawlerBuilderFactory.poll(mySource)
        .withFormat(customFormat)
        .ports(CrawlerPorts.builder()
                .inbox(myInboxWriter)
                .bus(myEventBus)
                .build())
        .create();

crawler.execute();
```

---

### Domain Event Mapping

Automatically deserialize each captured record into a domain event and emit it on the event bus.

```java
// Option A: The record itself implements Event — auto-deserialized with SNAKE_CASE naming
CrawlerStrategy crawler = CrawlerBuilderFactory.poll(mySource)
        .withFormat(Formats.JSON_ARRAY)
        .withDomainEvent(OrderReceived.class)
        .ports(ports)
        .create();

// Option B: Map a DTO to a domain event with a custom function
CrawlerStrategy crawler = CrawlerBuilderFactory.poll(mySource)
        .withFormat(Formats.JSON_ARRAY)
        .withNamingConvention(NamingConvention.CAMEL_CASE)
        .withDomainEvent(OrderDTO.class, (dto, key) ->
                new OrderReceived(dto.id(), dto.total(), key))
        .ports(ports)
        .create();
```

---

### Custom Ports (InboxWriter + EventBus)

Supply your own infrastructure adapters via `CrawlerPorts`:

```java
// Custom InboxWriter backed by a JPA repository
InboxWriter jpaWriter = (payload, idempotencyKey) -> {
    InboxEntity entity = new InboxEntity(payload, idempotencyKey.value());
    repository.save(entity);
    return idempotencyKey;
};

// Kafka-based EventBusEmitter
EventBusEmitter kafkaBus = event ->
    kafkaProducer.send(new ProducerRecord<>("spool-events",
            event.idempotencyKey().value(),
            objectMapper.writeValueAsString(event)));

CrawlerPorts ports = CrawlerPorts.builder()
        .inbox(jpaWriter)
        .bus(kafkaBus)
        .build();

CrawlerStrategy crawler = CrawlerBuilderFactory.poll(new GeminiTradeSource())
        .withFormat(Formats.JSON_ARRAY)
        .ports(ports)
        .create();

crawler.execute();
```

---

### Custom Error Routing

Override the default error routing to add logging, metrics, or custom recovery logic:

```java
ErrorRouter customRouter = new ErrorRouter()
        .on(SourcePollException.class, (e, cause) -> {
            logger.error("Source fetch failed: {}", e.getMessage());
            bus.emit(SourceFetchFailed.builder()
                    .errorMessage(e.getMessage()).build());
        })
        .on(DeserializationException.class, (e, cause) -> {
            logger.warn("Bad payload discarded: {}", e.getMessage());
        })
        .on(InboxWriteException.class, (e, cause) -> {
            metrics.increment("inbox.write.failures");
            bus.emit(InboxItemStoreFailed.builder()
                    .from(cause).errorMessage(e.getMessage()).build());
        });

CrawlerStrategy crawler = CrawlerBuilderFactory.poll(mySource)
        .withFormat(Formats.JSON_ARRAY)
        .ports(CrawlerPorts.builder()
                .inbox(myInboxWriter)
                .bus(myEventBus)
                .errorRouter(customRouter)
                .build())
        .create();

crawler.execute();
```

---

## API Reference

### Source Interfaces

| Interface | Method | Description |
|---|---|---|
| `PollSource<R>` | `R poll()` | Fetch the next payload on demand. |
| `PollSource<R>` | `PollSource<R> open()` | Set up connections before polling (optional). |
| `PollSource<R>` | `String sourceId()` | Unique identifier for the source. |
| `StreamSource<R>` | `void start(Consumer, Consumer)` | Start continuous streaming with callbacks. |
| `StreamSource<R>` | `void stop()` | Stop the stream and release resources. |
| `WebhookSource` | `WebhookRoute bindRoute()` | Return the HTTP route configuration. |

### Builder DSL

| Step | Method | Description |
|---|---|---|
| Entry point | `CrawlerBuilderFactory.poll(source)` | Start building a poll-based crawler. |
| Format | `.withFormat(Formats.JSON_ARRAY)` | Apply a pre-built processing pipeline. |
| Naming | `.withNamingConvention(NamingConvention.CAMEL_CASE)` | Set JSON field naming convention. |
| Domain Events | `.withDomainEvent(MyEvent.class)` | Register auto-deserialized domain events. |
| Ports | `.ports(CrawlerPorts.builder()...build())` | Provide inbox, bus, and error router. |
| Build | `.create()` | Return the configured `CrawlerStrategy`. |

---

## Built-in Formats

| Constant | Input Type | Intermediate | Output | Description |
|---|---|---|---|---|
| `Formats.JSON_ARRAY` | `String` (JSON) | `JsonNode` | `JsonNode` per element | Parse JSON array → split elements → serialize each to JSON. |
| `Formats.YAML_ARRAY` | `String` (YAML) | `JsonNode` | `JsonNode` per element | Parse YAML sequence → split elements → serialize each to JSON. |
| `Formats.RESULT_SET` | `ResultSet` | `ResultSet` | `Map<String, Object>` per row | Project each row → serialize to JSON. |

---

## Event Model

| Event | When Emitted |
|---|---|
| `SourceItemCaptured` | After a record is deserialized, split, and serialized. |
| `InboxItemStored` | After a record is successfully written to the inbox. |
| `SourceFetchFailed` | When `PollSource.poll()` or `open()` throws an exception. |
| `SourceItemCaptureFailed` | When deserialization, splitting, or serialization fails. |
| `InboxItemStoreFailed` | When `InboxWriter.receive()` throws an exception. |

---

## License

Distributed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
