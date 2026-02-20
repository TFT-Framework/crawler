package es.ulpgc.eii.spool.example.sagulpa.crawlers.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.ulpgc.eii.spool.domain.crawler.CrawlerSource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class GeminiTradeCrawlerSource implements CrawlerSource<GeminiTrade> {

    private static final String API_URL = "https://api.gemini.com/v1/trades/btcusd?limit_trades=10";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper   = new ObjectMapper();

    @Override
    public Stream<GeminiTrade> read() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .GET()
                    .build();

            String body = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();

            List<GeminiTrade> trades = new ArrayList<>();
            for (JsonNode node : mapper.readTree(body)) {
                trades.add(new GeminiTrade(
                        node.get("tid").asLong(),
                        node.get("price").asText(),
                        node.get("amount").asText(),
                        node.get("type").asText(),
                        Instant.ofEpochMilli(node.get("timestampms").asLong())
                ));
            }
            return trades.stream();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching trades from Gemini", e);
        }
    }
}