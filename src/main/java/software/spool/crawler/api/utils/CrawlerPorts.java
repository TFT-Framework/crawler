package software.spool.crawler.api.utils;

import software.spool.core.port.EventBusEmitter;
import software.spool.core.utils.ErrorRouter;
import software.spool.crawler.api.port.InboxWriter;

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
    private final ErrorRouter errorRouter;

    private CrawlerPorts(Builder builder) {
        this.inboxWriter = builder.inboxWriter;
        this.bus = builder.bus;
        this.errorRouter = builder.errorRouter;
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
     * Returns the {@link ErrorRouter} that handles exceptions during crawling.
     *
     * @return the error router; may be {@code null} to use the strategy default
     */
    public ErrorRouter errorRouter() {
        return errorRouter;
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
        private ErrorRouter errorRouter;

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
            this.inboxWriter = inboxWriter;
            return this;
        }

        /**
         * Sets the {@link EventBusEmitter} port.
         *
         * @param bus the event bus emitter to use
         * @return this builder for chaining
         */
        public Builder bus(EventBusEmitter bus) {
            this.bus = bus;
            return this;
        }

        /**
         * Sets the error router.
         *
         * @param errorRouter the error router to use; pass {@code null} to use the
         *                    strategy's default routing table
         * @return this builder for chaining
         */
        public Builder errorRouter(ErrorRouter errorRouter) {
            this.errorRouter = errorRouter;
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
