package software.spool.crawler.api.port;

import software.spool.core.exception.BusEmitException;
import software.spool.core.model.SpoolEvent;

/**
 * Port for publishing {@link SpoolEvent} instances to the application event
 * bus.
 *
 * <p>
 * Implement this interface to bridge the crawler to your event infrastructure
 * (Kafka, RabbitMQ, Spring ApplicationEvent, etc.). The crawler emits
 * lifecycle events such as {@code SourceItemCaptured} and
 * {@code InboxItemStored}
 * through this port after successfully processing each record.
 * </p>
 */
public interface EventBusEmitter {
    /**
     * Publishes the given event to the event bus.
     *
     * @param event the event to emit; must not be {@code null}
     * @throws BusEmitException if the event could not be published
     */
    void emit(SpoolEvent event) throws BusEmitException;
}
