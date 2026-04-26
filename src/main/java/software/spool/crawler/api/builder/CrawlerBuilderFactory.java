package software.spool.crawler.api.builder;

import software.spool.core.adapter.watchdog.HttpWatchdogClient;
import software.spool.core.model.watchdog.ModuleIdentity;
import software.spool.core.port.watchdog.ModuleHeartBeat;
import software.spool.core.utils.polling.PollingHeartbeat;
import software.spool.crawler.api.port.source.PollSource;

import java.util.Objects;

public final class CrawlerBuilderFactory {
    private CrawlerBuilderFactory() {}

    public static <R> PollingCrawlerBuilder<R> poll(PollSource<R> source) {
        return new Configuration().poll(source);
    }

    public static Configuration watchdog(String url, String moduleId) {
        return new Configuration(url, moduleId);
    }

    public static final class Configuration {
        private final String watchdogUrl;
        private final String moduleId;

        private Configuration(String watchdogUrl, String moduleId) {
            this.watchdogUrl = watchdogUrl;
            this.moduleId = moduleId;
        }

        private Configuration() {
            this(null, "crawler");
        }

        public <R> PollingCrawlerBuilder<R> poll(PollSource<R> source) {
            return new PollingCrawlerBuilder<>(source, buildHeartbeat(watchdogUrl, moduleId));
        }
    }

    private static ModuleHeartBeat buildHeartbeat(String watchdogUrl, String moduleId) {
        return Objects.isNull(watchdogUrl) ?
                ModuleHeartBeat.NOOP : new PollingHeartbeat(
                new HttpWatchdogClient(watchdogUrl),
                ModuleIdentity.of(moduleId)
        );
    }
}
