package software.spool.crawler.internal.control.steps;

import software.spool.core.model.event.SourcePayloadCaptured;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.pipeline.ContextKey;
import software.spool.crawler.internal.utils.TypedDomainMapping;
import java.util.Optional;

public final class CapturedPayloadKeys {
    private CapturedPayloadKeys() {}

    public static final ContextKey<String> SOURCE_ID = ContextKey.of("sourceId");
    public static final ContextKey<String> PAYLOAD = ContextKey.of("payload");
    public static final ContextKey<SourcePayloadCaptured> CAPTURED_EVENT = ContextKey.of("capturedEvent");
    public static final ContextKey<Optional<TypedDomainMapping>> DOMAIN_MAPPING = ContextKey.of("domainMapping");
    public static final ContextKey<IdempotencyKey> RECEIVED_KEY = ContextKey.of("receivedKey");
}