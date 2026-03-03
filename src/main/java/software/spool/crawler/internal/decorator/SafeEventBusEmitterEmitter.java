package software.spool.crawler.internal.decorator;

import software.spool.core.exception.BusEmitException;
import software.spool.core.exception.SpoolException;
import software.spool.core.model.SpoolEvent;
import software.spool.crawler.api.port.EventBusEmitter;

/**
 * Decorator for {@link EventBusEmitter} that normalises unchecked exceptions
 * into typed {@link BusEmitException} instances.
 *
 * <p>
 * If the delegate's {@link #emit(SpoolEvent)} method throws a
 * {@link SpoolException} subclass, it is re-thrown as-is. Any other
 * {@link Exception} is wrapped in a new {@link BusEmitException}.
 * </p>
 */
public class SafeEventBusEmitterEmitter implements EventBusEmitter {
    private final EventBusEmitter bus;

    private SafeEventBusEmitterEmitter(EventBusEmitter bus) {
        this.bus = bus;
    }

    /**
     * Creates a new {@code SafeEventBusEmitterEmitter} wrapping the given delegate.
     *
     * @param bus the event bus emitter to wrap; must not be {@code null}
     * @return a new {@code SafeEventBusEmitterEmitter} instance
     */
    public static SafeEventBusEmitterEmitter of(EventBusEmitter bus) {
        return new SafeEventBusEmitterEmitter(bus);
    }

    @Override
    public void emit(SpoolEvent event) throws BusEmitException {
        try {
            bus.emit(event);
        } catch (SpoolException e) {
            throw e;
        } catch (Exception e) {
            throw new BusEmitException("An error occurred while emitting an event: " + e.getMessage(), e);
        }
    }
}
