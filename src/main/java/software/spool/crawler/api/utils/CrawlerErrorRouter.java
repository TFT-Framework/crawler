package software.spool.crawler.api.utils;

import software.spool.core.exception.*;
import software.spool.core.model.failure.*;
import software.spool.core.port.bus.EventBusEmitter;
import software.spool.core.utils.routing.ErrorRouter;


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
    public static ErrorRouter defaults(EventBusEmitter bus) {
        return new ErrorRouter()
                .on(SourceOpenException.class,
                        (e, cause) -> bus.emit(SourceFetchFailed.builder()
                                .errorMessage(e.getMessage()).build()))
                .on(SourcePollException.class,
                        (e, cause) -> bus.emit(SourceFetchFailed.builder()
                                .errorMessage(e.getMessage()).build()))
                .on(DeserializationException.class,
                        (e, cause) -> bus.emit(SourceItemCaptureFailed.builder()
                                .errorMessage(e.getMessage()).build()))
                .on(SplitException.class,
                        (e, cause) -> bus.emit(SourceItemCaptureFailed.builder()
                                .errorMessage(e.getMessage()).build()))
                .on(SerializationException.class,
                        (e, cause) -> bus.emit(SourceItemCaptureFailed.builder()
                                .errorMessage(e.getMessage()).build()))
                .on(InboxWriteException.class,
                        (e, cause) -> bus.emit(InboxItemStoreFailed.builder()
                                .from(cause).errorMessage(e.getMessage()).build()))
                .orElse((e, cause) -> System.out.println(e.getMessage()));
    }
}
