package software.spool.crawler.internal.control.steps;

import software.spool.core.model.event.SourcePayloadCaptured;
import software.spool.core.pipeline.PipelineContext;
import software.spool.core.pipeline.Step;
import software.spool.core.port.bus.BrokerMessage;
import software.spool.core.port.bus.Destination;
import software.spool.core.port.bus.EventPublisher;

import javax.management.AttributeNotFoundException;
import java.util.Map;

public class PublishCapturedEvent implements Step<PipelineContext, PipelineContext> {
    private final EventPublisher publisher;

    public PublishCapturedEvent(EventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public PipelineContext apply(PipelineContext ctx) throws AttributeNotFoundException {
        SourcePayloadCaptured event = ctx.require(CapturedPayloadKeys.CAPTURED_EVENT);
        publisher.publish(
                new Destination("spool." + event.getClass().getSimpleName()),
                new BrokerMessage<>(event, event.getClass().getSimpleName(), Map.of()));
        return ctx;
    }
}
