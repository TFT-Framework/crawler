package software.spool.crawler.internal.control.pipeline.steps;

import software.spool.core.exception.DuplicateEventException;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.*;
import software.spool.core.port.serde.RecordSerializer;
import software.spool.crawler.api.port.InboxWriter;
import software.spool.crawler.internal.control.pipeline.Step;
import software.spool.crawler.internal.utils.TypedDomainMapping;

import javax.management.AttributeNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class BuildAndStoreEnvelopeStep implements Step<PipelineContext, PipelineContext> {
    private final InboxWriter inboxWriter;
    private final RecordSerializer<PartitionKeySchema> serializer;
    private final List<String> defaultPartitionAttributes;

    public BuildAndStoreEnvelopeStep(InboxWriter inboxWriter, RecordSerializer<PartitionKeySchema> serializer, List<String> defaultPartitionAttributes) {
        this.inboxWriter = inboxWriter;
        this.serializer = serializer;
        this.defaultPartitionAttributes = defaultPartitionAttributes;
    }

    @Override
    public PipelineContext apply(PipelineContext ctx) throws AttributeNotFoundException {
        try {
            IdempotencyKey key = inboxWriter.receive(buildEnvelope(ctx));
            if (key == null)
                throw new DuplicateEventException(ctx.require(CapturedPayloadKeys.CAPTURED_EVENT).idempotencyKey());
            return ctx.with(CapturedPayloadKeys.RECEIVED_KEY, key);
        } catch (Exception e) {
            throw e;
        }
    }

    private Envelope buildEnvelope(PipelineContext ctx) throws AttributeNotFoundException {
        return new Envelope(
                ctx.require(CapturedPayloadKeys.CAPTURED_EVENT).idempotencyKey(),
                buildMetadata(ctx),
                ctx.require(CapturedPayloadKeys.PAYLOAD),
                EnvelopeStatus.CAPTURED, 0, Instant.now());
    }

    private EventMetadata buildMetadata(PipelineContext ctx) throws AttributeNotFoundException {
        Optional<TypedDomainMapping> matched = ctx.require(CapturedPayloadKeys.DOMAIN_MAPPING);
        EventMetadata metadata = new EventMetadata()
                .set(EventMetadataKey.SOURCE, ctx.require(CapturedPayloadKeys.SOURCE_ID))
                .set(EventMetadataKey.PARTITION_SCHEMA, serializer.serialize(buildPartitionSchema(ctx)));
        matched.map(TypedDomainMapping::targetType)
                .ifPresent(type -> metadata.set(EventMetadataKey.TYPE, type.toString()));
        return metadata;
    }

    private PartitionKeySchema buildPartitionSchema(PipelineContext ctx) throws AttributeNotFoundException {
        Optional<TypedDomainMapping> matched = ctx.require(CapturedPayloadKeys.DOMAIN_MAPPING);
        return PartitionKeySchema.of(
                ctx.require(CapturedPayloadKeys.SOURCE_ID),
                matched.map(TypedDomainMapping::targetType).orElse(null),
                matched.map(TypedDomainMapping::partitionAttributes).orElse(defaultPartitionAttributes));
    }
}
