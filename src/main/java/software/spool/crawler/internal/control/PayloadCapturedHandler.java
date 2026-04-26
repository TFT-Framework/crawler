package software.spool.crawler.internal.control;

import software.spool.core.pipeline.Pipeline;
import software.spool.core.pipeline.PipelineContext;
import software.spool.core.port.bus.Handler;
import software.spool.core.utils.routing.ErrorRouter;
import software.spool.crawler.internal.control.steps.CapturedPayloadKeys;

public class PayloadCapturedHandler implements Handler<String> {
    private final Pipeline<PipelineContext, PipelineContext> pipeline;
    private final String sourceId;
    private final ErrorRouter errorRouter;

    public PayloadCapturedHandler(Pipeline<PipelineContext, PipelineContext> pipeline, String sourceId, ErrorRouter errorRouter) {
        this.pipeline = pipeline;
        this.sourceId = sourceId;
        this.errorRouter = errorRouter;
    }

    @Override
    public void handle(String payload) {
        PipelineContext initial = PipelineContext.empty()
                .with(CapturedPayloadKeys.SOURCE_ID, sourceId)
                .with(CapturedPayloadKeys.PAYLOAD, payload);
        pipeline.execute(initial)
                .peekError(errorRouter::dispatch);
    }
}