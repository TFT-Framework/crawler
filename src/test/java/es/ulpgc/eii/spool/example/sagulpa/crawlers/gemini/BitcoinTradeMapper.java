package es.ulpgc.eii.spool.example.sagulpa.crawlers.gemini;

import es.ulpgc.eii.spool.domain.EventCategory;
import es.ulpgc.eii.spool.domain.SchemaVersion;
import es.ulpgc.eii.spool.domain.crawler.EventDeserializer;
import java.util.UUID;

public class BitcoinTradeMapper implements EventDeserializer<GeminiTrade, BitcoinTradeEvent> {

    @Override
    public BitcoinTradeEvent deserialize(GeminiTrade trade) {
        return new BitcoinTradeEvent(
                UUID.randomUUID().toString(),
                "gemini-btcusd",
                "gemini-trade-" + trade.tid(),
                EventCategory.DOMAIN,
                "BITCOIN_TRADE_EXECUTED",
                trade.occurredAt(),
                SchemaVersion.of("1.0.0"),
                trade.price(),
                trade.amount(),
                trade.type()
        );
    }
}
