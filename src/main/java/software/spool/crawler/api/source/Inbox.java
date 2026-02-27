package software.spool.crawler.api.source;

import software.spool.model.RawDataReadFromSource;
import software.spool.crawler.api.exception.SpoolException;

@FunctionalInterface
public interface Inbox {
    InboxEntryId receive(RawDataReadFromSource event) throws SpoolException;
}