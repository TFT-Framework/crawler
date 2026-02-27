package software.spool.crawler.api;

import software.spool.model.SpoolEvent;

public interface EventBus {
    void emit(SpoolEvent event);
}
