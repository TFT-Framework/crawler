package software.spool.crawler.internal.port.decorator;

import software.spool.core.exception.SourcePollException;
import software.spool.core.exception.SpoolException;
import software.spool.crawler.api.port.source.PollSource;

/**
 * Decorator for {@link PollSource} that normalises unchecked exceptions into
 * typed {@link SourcePollException} instances.
 *
 * <p>
 * If the delegate's {@link #poll()} method throws a
 * {@link SpoolException} subclass, it is re-thrown as-is. Any other
 * {@link Exception} is wrapped in a new {@link SourcePollException}. This
 * guarantees the crawler strategy always receives typed exceptions that can be
 * handled by the {@link software.spool.core.utils.ErrorRouter ErrorRouter}.
 * </p>
 *
 * @param <R> the raw type produced by the wrapped source
 */
public class SafePollSource<R> implements PollSource<R> {
    private final PollSource<R> source;

    private SafePollSource(PollSource<R> source) {
        this.source = source;
    }

    /**
     * Creates a new {@code SafePollSource} wrapping the given delegate.
     *
     * @param <R>    the raw type produced by the source
     * @param source the source to wrap; must not be {@code null}
     * @return a new {@code SafePollSource} instance
     */
    public static <R> SafePollSource<R> of(PollSource<R> source) {
        return new SafePollSource<>(source);
    }

    @Override
    public R poll() throws SpoolException {
        try {
            return source.poll();
        } catch (SpoolException e) {
            throw e;
        } catch (Exception e) {
            throw new SourcePollException(sourceId(), e.getMessage(), e);
        }
    }

    @Override
    public String sourceId() {
        return source.sourceId();
    }
}
