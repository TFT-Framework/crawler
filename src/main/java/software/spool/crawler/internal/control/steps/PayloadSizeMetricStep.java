package software.spool.crawler.internal.control.steps;

import software.spool.core.pipeline.PipelineContext;
import software.spool.core.pipeline.Step;
import software.spool.core.port.metrics.MetricsRegistry;

import javax.management.AttributeNotFoundException;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PayloadSizeMetricStep implements Step<PipelineContext, PipelineContext> {
    private final MetricsRegistry.LongHistogramMetric histogram;

    public PayloadSizeMetricStep(MetricsRegistry.LongHistogramMetric histogram) {
        this.histogram = histogram;
    }

    @Override
    public PipelineContext apply(PipelineContext ctx) throws AttributeNotFoundException {
        String payload = ctx.require(CapturedPayloadKeys.PAYLOAD);
        histogram.record(payload.getBytes(UTF_8).length, Map.of());
        return ctx;
    }
}
