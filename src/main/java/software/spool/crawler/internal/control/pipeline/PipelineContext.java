package software.spool.crawler.internal.control.pipeline;

import javax.management.AttributeNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class PipelineContext {
    private final Map<ContextKey<?>, Object> values;

    private PipelineContext(Map<ContextKey<?>, Object> values) {
        this.values = Collections.unmodifiableMap(values);
    }

    public static PipelineContext empty() {
        return new PipelineContext(Map.of());
    }

    @SuppressWarnings("unchecked")
    public <T> T require(ContextKey<T> key) throws AttributeNotFoundException {
        Object val = values.get(key);
        if (val == null) throw new AttributeNotFoundException("There is no context for this key: " + key);
        return (T) val;
    }

    public <T> PipelineContext with(ContextKey<T> key, T value) {
        var newValues = new HashMap<>(values);
        newValues.put(key, value);
        return new PipelineContext(newValues);
    }
}