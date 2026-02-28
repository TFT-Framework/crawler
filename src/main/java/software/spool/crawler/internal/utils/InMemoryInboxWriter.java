package software.spool.crawler.internal.utils;

import software.spool.core.model.RawDataReadFromSource;
import software.spool.core.exception.SpoolException;
import software.spool.crawler.api.port.InboxEntryId;
import software.spool.crawler.api.port.InboxWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InMemoryInboxWriter implements InboxWriter {
    private final Map<UUID, RawDataReadFromSource> inbox;

    public InMemoryInboxWriter() {
        inbox = new HashMap<>();
    }

    @Override
    public InboxEntryId receive(RawDataReadFromSource event) throws SpoolException {
        UUID uuid = UUID.randomUUID();
        this.inbox.put(uuid, event);
        return new InboxEntryId(uuid.toString());
    }

    @Override
    public String toString() {
        return "InMemoryInbox{" +
                buildString() +
                '}';
    }

    private String buildString() {
        StringBuilder builder = new StringBuilder();
        inbox.forEach((key, value) -> {
            builder.append(key);
            builder.append(": ");
            builder.append(value);
            builder.append("\n");
        });
        return builder.toString();
    }
}
