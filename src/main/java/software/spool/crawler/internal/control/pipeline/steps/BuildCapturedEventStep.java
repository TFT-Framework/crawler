package software.spool.crawler.internal.control.pipeline.steps;

import software.spool.core.model.event.SourcePayloadCaptured;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.crawler.internal.control.pipeline.PipelineContext;
import software.spool.crawler.internal.control.pipeline.Step;

import javax.management.AttributeNotFoundException;
import java.util.UUID;

public class BuildCapturedEventStep implements Step<PipelineContext, PipelineContext> {
    @Override
    public PipelineContext apply(PipelineContext ctx) throws AttributeNotFoundException {
        SourcePayloadCaptured captured = SourcePayloadCaptured.builder()
                .idempotencyKey(IdempotencyKey.of(ctx.require(CapturedPayloadKeys.SOURCE_ID),
                        ctx.require(CapturedPayloadKeys.PAYLOAD)))
                .correlationId(UUID.randomUUID().toString())
                .build();
        return ctx.with(CapturedPayloadKeys.CAPTURED_EVENT, captured);
    }
}
