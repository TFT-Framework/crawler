package software.spool.crawler.internal.port;

/**
 * Base internal interface for all types of data sources.
 *
 * <p>
 * {@code Source} extends {@link AutoCloseable} so that sources can be used
 * inside try-with-resources blocks. The {@link #close()} method is a no-op by
 * default; implementors should override it when resources need to be released
 * after a polling or streaming cycle completes.
 * </p>
 *
 * <p>
 * This is an SPI interface and is not intended for direct implementation
 * by end users. Use the higher-level
 * {@link software.spool.crawler.api.source.PollSource},
 * {@link software.spool.crawler.api.source.StreamSource}, or
 * {@link software.spool.crawler.api.source.WebhookSource} instead.
 * </p>
 */
public interface Source extends AutoCloseable {
    /**
     * Returns the unique identifier of this source.
     *
     * @return a non-null string that uniquely identifies this source instance
     */
    String sourceId();

    /**
     * Releases any resources held by this source.
     *
     * <p>
     * The default implementation is a no-op. Override to close connections,
     * streams, or other resources.
     * </p>
     */
    default void close() {
    }
}
