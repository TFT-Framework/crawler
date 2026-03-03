package software.spool.crawler.api.strategy;

import software.spool.crawler.api.ErrorRouter;
import software.spool.core.exception.*;
import software.spool.crawler.api.port.EventBusEmitter;
import software.spool.core.model.*;

import java.util.Objects;

/**
 * Base implementation of {@link CrawlerStrategy} that provides a default
 * {@link ErrorRouter} wired to emit the appropriate
 * {@link software.spool.core.model.SpoolEvent} for each known exception
 * category.
 *
 * <p>
 * The default routing table handles the following exceptions:
 * <ul>
 * <li>{@link SourceOpenException} → emits {@code SourceFetchFailed}</li>
 * <li>{@link SourcePollException} → emits {@code SourceFetchFailed}</li>
 * <li>{@link DeserializationException} → emits
 * {@code SourceItemCaptureFailed}</li>
 * <li>{@link SourceSplitException} → emits {@code SourceItemCaptureFailed}</li>
 * <li>{@link SerializationException} → emits
 * {@code SourceItemCaptureFailed}</li>
 * <li>{@link InboxWriteException} → emits {@code InboxItemStoreFailed}</li>
 * </ul>
 * </p>
 *
 * <p>
 * Subclasses can supply a custom {@link ErrorRouter} to override or extend
 * this behaviour.
 * </p>
 */
public class BaseCrawlerStrategy implements CrawlerStrategy {
        /** Event bus used to publish error events. */
        private final EventBusEmitter bus;
        /** Logical name of the component acting as sender in emitted events. */
        private final String sender;
        /** Error router that maps exceptions to event bus emissions. */
        protected final ErrorRouter errorRouter;
        /** Identifier of the data source being crawled. */
        private final String sourceId;

        /**
         * Creates a new strategy base with the given ports and optional custom router.
         *
         * @param bus         the event bus emitter used in the default error routing;
         *                    must not be {@code null}
         * @param sourceId    the identifier of the crawled source; used in emitted
         *                    events
         * @param sender      the logical sender name included in emitted events
         * @param errorRouter a custom {@link ErrorRouter}, or {@code null} to use the
         *                    default routing table
         */
        public BaseCrawlerStrategy(EventBusEmitter bus, String sourceId, String sender, ErrorRouter errorRouter) {
                this.bus = bus;
                this.sourceId = sourceId;
                this.sender = sender;
                this.errorRouter = Objects.isNull(errorRouter) ? initializeErrorRouter() : errorRouter;
        }

        private ErrorRouter initializeErrorRouter() {
                return new ErrorRouter().on(SourceOpenException.class,
                                (e, cause) -> bus.emit(SourceFetchFailed.builder()
                                                .senderId(sender)
                                                .sourceId(sourceId)
                                                .errorMessage(e.getMessage()).build()))
                                .on(SourcePollException.class,
                                                (e, cause) -> bus.emit(SourceFetchFailed.builder()
                                                                .senderId(sender)
                                                                .sourceId(sourceId)
                                                                .errorMessage(e.getMessage()).build()))
                                .on(DeserializationException.class,
                                                (e, cause) -> bus.emit(SourceItemCaptureFailed.builder()
                                                                .senderId(sender)
                                                                .sourceId(sourceId)
                                                                .errorMessage(e.getMessage()).build()))
                                .on(SourceSplitException.class,
                                                (e, cause) -> bus.emit(SourceItemCaptureFailed.builder()
                                                                .senderId(sender)
                                                                .sourceId(sourceId)
                                                                .errorMessage(e.getMessage()).build()))
                                .on(SerializationException.class,
                                                (e, cause) -> bus.emit(SourceItemCaptureFailed.builder()
                                                                .senderId(sender)
                                                                .sourceId(sourceId)
                                                                .errorMessage(e.getMessage()).build()))
                                .on(InboxWriteException.class,
                                                (e, cause) -> bus.emit(InboxItemStoreFailed.builder()
                                                                .from(cause)
                                                                .sourceId(sourceId)
                                                                .senderId(sender)
                                                                .errorMessage(e.getMessage()).build()));
        }

        @Override
        public void execute() throws SpoolException {
        }
}
