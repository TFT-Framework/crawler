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

    public static Configuration watchdogUrl(String url) {
        return new Configuration(url);
    }

    public static final class Configuration {
        private final String watchdogUrl;

        private Configuration(String watchdogUrl) {
            this.watchdogUrl = watchdogUrl;
        }

        private Configuration() {
            this(null);
        }

        public <R> PollingCrawlerBuilder<R> poll(PollSource<R> source) {
            return new PollingCrawlerBuilder<>(source, buildHeartbeat(watchdogUrl));
        }
    }

    private static ModuleHeartBeat buildHeartbeat(String watchdogUrl) {
        return Objects.isNull(watchdogUrl) ?
                ModuleHeartBeat.NOOP : new PollingHeartbeat(
                new HttpWatchdogClient(watchdogUrl),
                ModuleIdentity.random("crawler")
        );
    }
}
