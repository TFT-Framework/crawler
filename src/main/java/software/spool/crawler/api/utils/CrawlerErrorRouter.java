package software.spool.crawler.api.utils;

import software.spool.core.adapter.logging.LoggerFactory;
import software.spool.core.port.bus.EventPublisher;
import software.spool.core.port.logging.Logger;
import software.spool.core.utils.routing.ErrorRouter;
import software.spool.crawler.api.Crawler;


public class CrawlerErrorRouter {

    private CrawlerErrorRouter() {
        // utility class
    }

    /**
     * Creates the default {@link ErrorRouter} wired to the given event bus.
     *
     * @param bus the event bus emitter used for publishing failure events
     * @return a pre-configured error router
     */
    public static ErrorRouter defaults(EventPublisher bus) {
        Logger log = LoggerFactory.getLogger(Crawler.class);
        return new ErrorRouter()
                .orElse((e, cause) -> log.error(e.getMessage()));
    }
}
