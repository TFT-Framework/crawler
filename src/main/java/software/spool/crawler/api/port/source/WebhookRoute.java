package software.spool.crawler.api.port.source;

/**
 * Configuration descriptor for registering a webhook HTTP endpoint.
 *
 * <p>
 * A {@link WebhookSource} returns an instance of this class from
 * {@link WebhookSource#bindRoute()} to tell the crawler runtime which HTTP
 * method, path, and other routing options should be used when exposing the
 * webhook. Extend or populate this class with the fields required by your
 * HTTP framework integration.
 * </p>
 */
public class WebhookRoute {
    /** Creates a new {@code WebhookRoute}. */
    public WebhookRoute() {
    }
}
