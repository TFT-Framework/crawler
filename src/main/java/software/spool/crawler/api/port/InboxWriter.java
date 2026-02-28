package software.spool.crawler.api.port;

import software.spool.core.exception.InboxWriteException;
import software.spool.core.model.RawDataReadFromSource;

public interface InboxWriter {
    InboxEntryId receive(RawDataReadFromSource event) throws InboxWriteException;
}
