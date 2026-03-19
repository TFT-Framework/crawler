package software.spool.crawler.internal.control;

import software.spool.core.port.Subscription;

public final class CrawlerSubscription implements Subscription {
    private volatile boolean active = true;

    @Override
    public boolean isActive() { return active; }

    @Override
    public void cancel() {
        active = false;
    }
}