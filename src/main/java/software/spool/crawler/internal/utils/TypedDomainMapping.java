package software.spool.crawler.internal.utils;


import software.spool.core.utils.serialization.DomainEventMapping;

import java.util.List;

public record TypedDomainMapping(Class<?> targetType, DomainEventMapping<?> mapping, List<String> partitionAttributes) {
    public static TypedDomainMapping of(Class<?> type, DomainEventMapping<?> mapping, String... partitionAttributes) {
        return new TypedDomainMapping(type, mapping, List.of(partitionAttributes));
    }
}

