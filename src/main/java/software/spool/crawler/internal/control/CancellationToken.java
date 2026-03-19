package software.spool.crawler.internal.control;

public class CancellationToken {
    private volatile boolean cancelled = false;

    public boolean isCancelled() { return cancelled; }

    public boolean isActive() { return !cancelled; }

    public void cancel() { cancelled = true; }

    public void awaitCancellation() {
        while (!cancelled) {
            try { Thread.sleep(100); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
    }

    public static CancellationToken create() {
        return new CancellationToken();
    }

    public static final CancellationToken NONE = new CancellationToken() {
        @Override public boolean isCancelled() { return true; }
        @Override public boolean isActive() { return false; }
        @Override public void cancel() {}
    };
}
