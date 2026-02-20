package es.ulpgc.eii.spool.example.sagulpa.crawlers.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.ulpgc.eii.spool.domain.crawler.StreamSource;
import es.ulpgc.eii.spool.example.sagulpa.OccupancyRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.function.Consumer;

public class SagulpaKafkaCrawlerSource implements StreamSource<OccupancyRecord> {

    private final KafkaConsumer<String, String> consumer;
    private final Duration pollTimeout;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private volatile boolean running;

    public SagulpaKafkaCrawlerSource(KafkaConsumer<String, String> consumer, Duration pollTimeout) {
        this.consumer    = consumer;
        this.pollTimeout = pollTimeout;
    }

    @Override
    public void start(Consumer<OccupancyRecord> onMessage, Consumer<Exception> onError) {
        running = true;
        Thread thread = new Thread(() -> {
            while (running) {
                try {
                    consumer.poll(pollTimeout).forEach(record -> {
                        try {
                            OccupancyRecord raw = objectMapper.readValue(record.value(), OccupancyRecord.class);
                            onMessage.accept(raw);
                        } catch (Exception e) {
                            onError.accept(e);
                        }
                    });
                } catch (Exception e) {
                    onError.accept(e);
                }
            }
        }, "kafka-crawler-thread");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void stop() { running = false; }
}
