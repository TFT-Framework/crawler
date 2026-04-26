package software.spool.crawler.internal.control.steps;

import software.spool.core.pipeline.PipelineContext;
import software.spool.core.pipeline.Step;
import software.spool.crawler.internal.utils.DomainEventEmitter;
import software.spool.crawler.internal.utils.TypedDomainMapping;

import javax.management.AttributeNotFoundException;
import java.util.Optional;

public class PublishDomainEventStep implements Step<PipelineContext, PipelineContext> {
    private final DomainEventEmitter domainEventEmitter;

    public PublishDomainEventStep(DomainEventEmitter domainEventEmitter) {
        this.domainEventEmitter = domainEventEmitter;
    }

    @Override
    public PipelineContext apply(PipelineContext ctx) throws AttributeNotFoundException {
        Optional<TypedDomainMapping> matched = domainEventEmitter.emit(
                ctx.require(CapturedPayloadKeys.PAYLOAD),
                ctx.require(CapturedPayloadKeys.CAPTURED_EVENT).idempotencyKey());
        return ctx.with(CapturedPayloadKeys.DOMAIN_MAPPING, matched);
    }
}
