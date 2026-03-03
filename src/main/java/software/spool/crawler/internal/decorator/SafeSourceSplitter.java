package software.spool.crawler.internal.decorator;

import software.spool.core.exception.SourceSplitException;
import software.spool.core.exception.SpoolException;
import software.spool.crawler.internal.port.SourceSplitter;

import java.util.stream.Stream;

/**
 * Decorator for {@link SourceSplitter} that normalises unchecked exceptions
 * into typed {@link SourceSplitException} instances.
 *
 * <p>
 * If the delegate's {@link #split(Object, String)} method throws a
 * {@link SpoolException} subclass, it is re-thrown as-is. Any other
 * {@link Exception} is wrapped in a new {@link SourceSplitException}.
 * </p>
 *
 * @param <I> the intermediate input type
 * @param <O> the individual record output type
 */
public class SafeSourceSplitter<I, O> implements SourceSplitter<I, O> {
    private final SourceSplitter<I, O> splitter;

    private SafeSourceSplitter(SourceSplitter<I, O> splitter) {
        this.splitter = splitter;
    }

    /**
     * Creates a new {@code SafeSourceSplitter} wrapping the given delegate.
     *
     * @param <I>      the input type
     * @param <O>      the output type
     * @param splitter the splitter to wrap; must not be {@code null}
     * @return a new {@code SafeSourceSplitter} instance
     */
    public static <I, O> SafeSourceSplitter<I, O> of(SourceSplitter<I, O> splitter) {
        return new SafeSourceSplitter<>(splitter);
    }

    @Override
    public Stream<O> split(I payload, String sourceId) throws SpoolException {
        try {
            return splitter.split(payload, sourceId);
        } catch (SpoolException e) {
            throw e;
        } catch (Exception e) {
            throw new SourceSplitException(e.getMessage(), payload.toString());
        }
    }
}
