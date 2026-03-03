package software.spool.crawler.internal.port;

import software.spool.core.exception.SpoolException;

import java.util.stream.Stream;

/**
 * Internal SPI for splitting a parsed intermediate object into zero or more
 * individual records.
 *
 * <p>
 * This is the second stage of the three-stage processing pipeline:
 * deserialize → <b>split</b> → serialize.
 * </p>
 *
 * <p>
 * Pre-built implementations are available via
 * {@link software.spool.crawler.internal.utils.factory.SplitterFactory}.
 * </p>
 *
 * @param <I> the intermediate type produced by the deserializer
 * @param <O> the individual record type produced by splitting
 */
@FunctionalInterface
public interface SourceSplitter<I, O> {
    /**
     * Splits the parsed payload into a stream of individual records.
     *
     * @param payload  the intermediate data to split; must not be {@code null}
     * @param sourceId the identifier of the source that produced the payload;
     *                 useful for constructing error messages
     * @return a non-null (possibly empty) {@link Stream} of individual records
     * @throws SpoolException if the payload structure is incompatible with this
     *                        splitter
     */
    Stream<O> split(I payload, String sourceId) throws SpoolException;
}
