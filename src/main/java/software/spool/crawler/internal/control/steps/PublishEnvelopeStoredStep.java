package software.spool.crawler.internal.control.steps;

import software.spool.core.model.event.EnvelopeStored;
import software.spool.core.pipeline.PipelineContext;
import software.spool.core.pipeline.Step;
import software.spool.core.port.bus.BrokerMessage;
import software.spool.core.port.bus.Destination;
import software.spool.core.port.bus.EventPublisher;

import javax.management.AttributeNotFoundException;
import java.util.Map;

public class PublishEnvelopeStoredStep implements Step<PipelineContext, PipelineContext> {
    private final EventPublisher publisher;

    public PublishEnvelopeStoredStep(EventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public PipelineContext apply(PipelineContext ctx) throws AttributeNotFoundException {
        EnvelopeStored storedEvent = EnvelopeStored.builder()
                .from(ctx.require(CapturedPayloadKeys.CAPTURED_EVENT))
                .build();
        publisher.publish(
                new Destination("spool." + storedEvent.getClass().getSimpleName()),
                new BrokerMessage<>(storedEvent, storedEvent.getClass().getSimpleName(), Map.of()));
        return ctx;
    }
}