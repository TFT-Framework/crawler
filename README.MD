# spool-crawler

> A lightweight Java library for event ingestion from heterogeneous sources.
> Pull, Stream, and Webhook crawling strategies — all unified under a single fluent API.

[![Maven Central](https://img.shields.io/maven-central/v/es.ulpgc.eii/spool-crawler)](https://central.sonatype.com/artifact/es.ulpgc.eii/spool-crawler)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17+-brightgreen.svg)](https://openjdk.org/)

## Table of Contents

- [About](#about)
- [Installation](#installation)
- [Core Concepts](#core-concepts)
- [Usage](#usage)
    - [1. Define your Event](#1-define-your-event)
    - [2. Pull Strategy](#2-pull-strategy)
    - [3. Stream Strategy](#3-stream-strategy)
    - [4. Webhook Strategy](#4-webhook-strategy)
- [Docker](#docker)
- [License](#license)

---

## About

**spool-crawler** is a Java abstraction layer for ingesting events from multiple source types.
It decouples the *how* of data collection from the *what* of event processing,
making it easy to swap sources without touching business logic.

| Strategy | Transport        | Use case                               |
|----------|------------------|----------------------------------------|
| Pull     | HTTP, JDBC, file | Polling REST APIs or databases on demand |
| Stream   | Kafka, AMQP      | Continuous real-time event streams     |
| Webhook  | HTTP push        | Receiving external push notifications  |

---

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
<groupId>es.ulpgc.eii</groupId>
<artifactId>spool-crawler</artifactId>
<version>1.0.0</version>
</dependency>
```

> Requires **Java 17+** and **Maven 3.6+**.

---

## Core Concepts

| Class / Interface           | Role                                                                |
|-----------------------------|---------------------------------------------------------------------|
| `PlatformEvent`             | Interface that every platform event implements                      |
| `DomainEvent`               | Base interface every domain event must implement                    |
| `EventBuffer<T>`            | Thread-safe in-memory queue; holds events until `drain()` is called |
| `SourceType`                | Enum — `PULL`, `STREAM`, `WEBHOOK`                                  |
| `EventCategory`             | Enum — `PLATFORM` or `DOMAIN`                                       |
| `SchemaVersion`             | Semver-validated record (e.g. `"1.0.0"`)                            |
| `CrawlerSource<R>`          | Read-side abstraction for Pull sources — returns `Stream<R>`        |
| `StreamSource<R>`           | Push-side abstraction for Stream sources — `start()` / `stop()`     |
| `EventSource<R>`            | Abstractions for every source                                       |
| `PlatformEventSource`       | Abstractions for every source of platform events                    |
| `PullCrawlerStrategy<T>`    | Strategy abstraction for pull sources                               |
| `StreamCrawlerStrategy<T>`  | Strategy abstraction for stream sources                             |
| `WebhookCrawlerStrategy<T>` | Strategy abstraction for webhook sources                            |
| `EventDeserializer<R, T>`   | Converts a raw payload `R` into a typed `Event` subtype `T`         |

---

## Usage

### 1. Define your Event

Implement the `DomainEvent` interface for your domain:

```java
public record ParkingEvent(
    String id,
    String correlationId,
    String idempotencyKey,
    String lotId,
    int freeSpots,
    String status,
    Instant occurredAt
) implements DomainEvent {

    @Override public EventCategory eventCategory() { return EventCategory.DOMAIN; }
    @Override public String eventType()            { return "parking.occupancy.updated"; }
    @Override public SchemaVersion schemaVersion() { return SchemaVersion.of("1.0.0"); }
}
```

---

### 2. Pull Strategy

Use `PullCrawlerStrategy` to poll a source on demand (REST API, database, file, etc.).

```java
// 1. Implement CrawlerSource — fetches raw data from your source
CrawlerSource<String> apiSource = () -> httpClient.getLines("/api/parking/occupancy"); // returns Stream<String>

// 2. Implement EventDeserializer — maps raw data to your DomainEvent
EventDeserializer<String, ParkingEvent> deserializer = raw -> JsonMapper.parse(raw, ParkingEvent.class);

// 3. Build the strategy (fluent API)
PullCrawlerStrategy<ParkingEvent> strategy = PullCrawlerStrategy
    .from(apiSource)
    .onError(e -> System.out.println("An error occurred"))
    .deserializeWith(deserializer);
    .onEvent(e -> System.out.println("Event received: " + e.id()))
    .build();

// 4. Collect — returns a Stream<ParkingEvent>
strategy.collect().forEach(e -> System.out.println(e.lotId() + " → " + e.freeSpots() + " free spots"));
```

---

### 3. Stream Strategy

Use `StreamCrawlerStrategy` for continuous sources like Kafka or WebSockets.
Events are buffered internally and drained on demand via `crawl()`.

```java
// 1. Implement StreamSource — wraps a Kafka consumer, WebSocket, etc.
StreamSource<String> kafkaSource = new KafkaStreamSource("parking-events");

// 2. Build the strategy
StreamCrawlerStrategy<ParkingEvent> strategy = StreamCrawlerStrategy.from(kafkaSource)
    .deserializeWith(raw -> JsonMapper.parse(raw, ParkingEvent.class))
    .onEvent(event -> log.info("Received: {}", event.eventType()))
    .onError(ex  -> log.error("Stream error", ex))
    .build();

// 3. Start the background listener
strategy.start();

// 4. Drain buffered events (e.g. inside a @Scheduled task)
strategy.crawl().forEach(e -> process(e));

// 5. Stop when done
strategy.stop();
```

---

### 4. Webhook Strategy

Use `WebhookCrawlerStrategy` when an external system pushes payloads to your application
(e.g. an HTTP controller endpoint).

```java
// 1. Build the strategy
WebhookCrawlerStrategy<String, ParkingEvent> strategy = WebhookCrawlerStrategy.from(String.class)
    .deserializeWith(raw -> JsonMapper.parse(raw, ParkingEvent.class))
    .onEvent(event -> log.info("Webhook received: {}", event.id()))
    .build();

// 2. Forward raw payloads from your HTTP controller
@PostMapping("/webhook/parking")
public ResponseEntity<Void> onWebhook(@RequestBody String payload) {
    strategy.receive(payload); // deserializes and buffers the event
    return ResponseEntity.ok().build();
}

// 3. Drain buffered events
strategy.crawl().forEach(e -> process(e));
```

---

## License

Licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt).

© ULPGC EII — Javier Castilla