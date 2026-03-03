package software.spool.crawler.api.source;

import software.spool.crawler.internal.port.Source;

/**
 * A data source that receives data by exposing an HTTP webhook endpoint.
 *
 * <p>
 * Implementations must provide a {@link WebhookRoute} through
 * {@link #bindRoute()} so that the crawler runtime can register the appropriate
 * HTTP handler and forward incoming requests to the processing pipeline.
 * </p>
 */
public interface WebhookSource extends Source {
    /**
     * Returns the route configuration that defines how the webhook endpoint is
     * registered in the HTTP framework.
     *
     * @return the {@link WebhookRoute} for this source; must not be {@code null}
     */
    WebhookRoute bindRoute();
}
