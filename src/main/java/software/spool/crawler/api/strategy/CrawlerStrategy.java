package software.spool.crawler.api.strategy;

import software.spool.core.exception.SpoolException;

public interface CrawlerStrategy {
    void execute() throws SpoolException;
}
