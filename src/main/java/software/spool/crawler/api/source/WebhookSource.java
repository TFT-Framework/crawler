package software.spool.crawler.api.source;

import software.spool.crawler.api.Source;

public interface WebhookSource extends Source {
    WebhookRoute bindRoute();
}
