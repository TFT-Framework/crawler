package software.spool.crawler.example.gemini.application.crawler;

import software.spool.core.exception.*;
import software.spool.crawler.api.source.PollSource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeminiTradeCrawlerSource implements PollSource<String> {
    private static final String API_URL = "https://api.gemini.com/v1/trades/btcusd?limit_trades=10";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public String poll() throws SpoolException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .GET()
                    .build();

            return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new SourcePollException("Error fetching trades from Gemini", e);
        }
    }

    @Override
    public String sourceId() {
        return "geminiTradeCrawlerSource";
    }
}
