package software.spool.crawler.internal.utils;

import software.spool.core.utils.DomainEventMapping;

public record TypedDomainMapping(Class<?> targetType, DomainEventMapping<?> mapping) {}

