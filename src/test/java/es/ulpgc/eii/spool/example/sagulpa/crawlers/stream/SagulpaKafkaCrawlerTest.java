package es.ulpgc.eii.spool.example.sagulpa.crawlers.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.ulpgc.eii.spool.domain.crawler.strategy.StreamCrawlerStrategy;
import es.ulpgc.eii.spool.example.sagulpa.OccupancyRecord;
import es.ulpgc.eii.spool.example.sagulpa.ParkingEvent;
import es.ulpgc.eii.spool.example.sagulpa.ParkingEventMapper;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SagulpaKafkaCrawlerTest {

    private static final String BOOTSTRAP = "localhost:9092";

    private String topic;
    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, String> consumer;
    private StreamCrawlerStrategy<ParkingEvent> crawler;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        topic = "sagulpa.occupancy." + System.currentTimeMillis();

        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers", BOOTSTRAP);
        producerProps.put("key.serializer",    "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer",  "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(producerProps);

        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers",  BOOTSTRAP);
        consumerProps.put("group.id",           "spool-test-" + System.currentTimeMillis());
        consumerProps.put("key.deserializer",   "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("auto.offset.reset",  "earliest");

        consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(List.of(topic));
        crawler = StreamCrawlerStrategy
                .from(new SagulpaKafkaCrawlerSource(consumer, Duration.ofMillis(200)))
                .deserializeWith(new ParkingEventMapper())
                .build();
    }

    @AfterEach
    void tearDown() {
        crawler.stop();
        producer.close();
    }

    @Test
    void crawl_shouldConsumeOccupancyEventsFromKafka() throws Exception {
        crawler.start();

        producer.send(new ProducerRecord<>(topic, toJson(
                new OccupancyRecord(1, "elder",    150, Instant.now(), "OPEN")))).get();
        producer.send(new ProducerRecord<>(topic, toJson(
                new OccupancyRecord(2, "metropol",   0, Instant.now(), "FULL")))).get();

        List<ParkingEvent> accumulated = new ArrayList<>();
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            accumulated.addAll(crawler.crawl().toList());
            return accumulated.size() >= 2;
        });

        assertEquals(2, accumulated.size());

        accumulated.stream().forEach(System.out::println);
    }

    private String toJson(OccupancyRecord record) throws Exception {
        return objectMapper.writeValueAsString(record);
    }
}
