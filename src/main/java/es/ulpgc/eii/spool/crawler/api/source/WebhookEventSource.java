package es.ulpgc.eii.spool.crawler.api.source;

import es.ulpgc.eii.spool.crawler.api.EventSource;

public interface WebhookEventSource extends EventSource {
    WebhookRoute bindRoute();
}
