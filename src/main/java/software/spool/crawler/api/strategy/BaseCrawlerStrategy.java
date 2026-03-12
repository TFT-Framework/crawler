package software.spool.crawler.api.strategy;

import software.spool.core.port.EventBusEmitter;
import software.spool.core.utils.ErrorRouter;
import software.spool.crawler.api.utils.CrawlerErrorRouter;
import software.spool.core.exception.*;

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
 * <li>{@link SplitException} → emits {@code SourceItemCaptureFailed}</li>
 * <li>{@link SerializationException} → emits
 * {@code SourceItemCaptureFailed}</li>
 * <li>{@link InboxWriteException} → emits {@code InboxItemStoreFailed}</li>
 * </ul>
 *
 * <p>
 * Subclasses can supply a custom {@link ErrorRouter} to override or extend
 * this behaviour.
 * </p>
 */
public abstract class BaseCrawlerStrategy implements CrawlerStrategy {
        /** Event bus used to publish error events. */
        private final EventBusEmitter bus;
        /** Error router that maps exceptions to event bus emissions. */
        protected final ErrorRouter errorRouter;

        /**
         * Creates a new strategy base with the given ports and optional custom router.
         *
         * @param bus         the event bus emitter used in the default error routing;
         *                    must not be {@code null}
         * @param errorRouter a custom {@link ErrorRouter}, or {@code null} to use the
         *                    default routing table
         */
        public BaseCrawlerStrategy(EventBusEmitter bus, ErrorRouter errorRouter) {
                this.bus = bus;
                this.errorRouter = Objects.isNull(errorRouter) ? CrawlerErrorRouter.defaults(bus) : errorRouter;
        }

        @Override
        public abstract void execute() throws SpoolException;
}
