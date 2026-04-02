package software.spool.crawler.api.utils;

import software.spool.core.port.bus.EventBusEmitter;
import software.spool.core.port.decorator.SafeEventBusEmitter;
import software.spool.crawler.api.port.InboxWriter;
import software.spool.crawler.internal.port.decorator.SafeInboxWriter;

/**
 * Aggregates the external ports required by a crawler strategy into a single
 * immutable value object.
 *
 * <p>
 * Use {@link #builder()} to construct instances:
 * </p>
 * 
 * <pre>{@code
 * CrawlerPorts ports = CrawlerPorts.builder()
 *         .bus(myEventBus)
 *         .inbox(myInboxWriter)
 *         .errorRouter(myErrorRouter)
 *         .build();
 * }</pre>
 */
public class CrawlerPorts {
    private final InboxWriter inboxWriter;
    private final EventBusEmitter bus;

    private CrawlerPorts(Builder builder) {
        this.inboxWriter = builder.inboxWriter;
        this.bus = builder.bus;
    }

    /**
     * Returns the {@link InboxWriter} port.
     *
     * @return the inbox writer; may be {@code null} if not configured
     */
    public InboxWriter inboxWriter() {
        return inboxWriter;
    }

    /**
     * Returns the {@link EventBusEmitter} port.
     *
     * @return the event bus emitter; may be {@code null} if not configured
     */
    public EventBusEmitter bus() {
        return bus;
    }


    /**
     * Returns a new {@link Builder} for constructing a {@code CrawlerPorts}
     * instance.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link CrawlerPorts}.
     */
    public static class Builder {
        private InboxWriter inboxWriter;
        private EventBusEmitter bus;

        /** Creates a new empty builder. */
        Builder() {
        }

        /**
         * Sets the {@link InboxWriter} port.
         *
         * @param inboxWriter the inbox writer to use
         * @return this builder for chaining
         */
        public Builder inbox(InboxWriter inboxWriter) {
            this.inboxWriter = SafeInboxWriter.of(inboxWriter);
            return this;
        }

        /**
         * Sets the {@link EventBusEmitter} port.
         *
         * @param bus the event bus emitter to use
         * @return this builder for chaining
         */
        public Builder bus(EventBusEmitter bus) {
            this.bus = SafeEventBusEmitter.of(bus);
            return this;
        }

        /**
         * Builds and returns the configured {@link CrawlerPorts} instance.
         *
         * @return a new {@code CrawlerPorts}
         */
        public CrawlerPorts build() {
            return new CrawlerPorts(this);
        }
    }
}
