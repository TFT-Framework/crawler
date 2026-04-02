package software.spool.crawler.dsl;

import software.spool.core.adapter.kafka.KafkaEventBusConfig;
import software.spool.core.adapter.kafka.KafkaEventBusEmitter;
import software.spool.core.adapter.kafka.KafkaEventBusListener;
import software.spool.core.adapter.memory.InMemoryEventBus;
import software.spool.core.port.bus.EventBus;
import software.spool.core.port.bus.EventBusEmitter;
import software.spool.core.port.bus.EventBusListener;

class EventBusFactory {
    protected static EventBus console() {
        return new InMemoryEventBus();
    }

    protected static EventBusEmitter kafkaEmitter(String url) {
        return new KafkaEventBusEmitter(new KafkaEventBusConfig(url));
    }

    protected static EventBusListener kafkaListener(String url) {
        return new KafkaEventBusListener(new KafkaEventBusConfig(url));
    }

    protected static EventBusEmitter activeMQEmitter() {
        return null;
    }

    protected static EventBusEmitter rabbitMQEmitter() {
        return null;
    }
}
