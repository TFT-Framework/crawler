package software.spool.crawler.internal.control.pipeline;

import software.spool.core.adapter.logging.LoggerFactory;
import software.spool.core.port.logging.Logger;

import javax.management.AttributeNotFoundException;

public class ObservedStep<I, O> implements Step<I, O> {
    private final String name;
    private final Step<I, O> delegate;
    private static final Logger LOGGER = LoggerFactory.getLogger(ObservedStep.class);

    public ObservedStep(String name, Step<I, O> delegate) {
        this.name = name;
        this.delegate = delegate;
    }

    @Override
    public O apply(I input) throws AttributeNotFoundException {
        try {
            return delegate.apply(input);
        } catch (Exception e) {
            LOGGER.error("Step failed: " + name);
            throw e;
        } finally {
            LOGGER.info("Step " + "'" + name + "'" + " finished");
        }
    }
}