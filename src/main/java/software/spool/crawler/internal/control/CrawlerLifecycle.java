package software.spool.crawler.internal.control;

import software.spool.core.port.Subscription;

public interface CrawlerLifecycle {
    void onReady(Subscription subscription);
    void onCompleted();
    void onError(Exception e);
}
