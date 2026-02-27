package software.spool.crawler.internal.utils;

import software.spool.model.RawDataReadFromSource;
import software.spool.crawler.api.exception.SpoolException;
import software.spool.crawler.api.source.Inbox;
import software.spool.crawler.api.source.InboxEntryId;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InMemoryInbox implements Inbox {
    private final Map<UUID, RawDataReadFromSource> inbox;

    public InMemoryInbox() {
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
