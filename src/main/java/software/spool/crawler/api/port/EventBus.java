package software.spool.crawler.api.port;

import software.spool.core.exception.BusEmitException;
import software.spool.core.model.SpoolEvent;

public interface EventBus {
    void emit(SpoolEvent event) throws BusEmitException;
}
