package com.mvp18.trading_challenge_backend.service;

import reactor.netty.http.client.HttpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MarketDataService {

    @Value("${binance.base.url}")
    private String binanceBaseUrl;

    private final ConcurrentHashMap<String, Double> priceCache
            = new ConcurrentHashMap<>();

    private WebClient binanceClient;
    private WebClient frankfurterClient;
    private WebClient goldClient;

    @PostConstruct
    public void init() {
        binanceClient = WebClient.builder()
                .baseUrl(binanceBaseUrl).build();

        frankfurterClient = WebClient.builder()
                .baseUrl("https://api.frankfurter.app")
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().followRedirect(true)))
                .build();

        goldClient = WebClient.builder()
                .baseUrl("https://api.gold-api.com")
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().followRedirect(true)))
                .build();

        // ===== DEFAULT PRICES =====

        // Forex Majors
        priceCache.put("EURUSD", 1.0854);
        priceCache.put("GBPUSD", 1.2734);
        priceCache.put("USDJPY", 154.32);
        priceCache.put("USDCHF", 0.9012);
        priceCache.put("AUDUSD", 0.6543);
        priceCache.put("USDCAD", 1.3654);
        priceCache.put("NZDUSD", 0.6012);

        // Forex Minors
        priceCache.put("EURGBP", 0.8521);
        priceCache.put("EURJPY", 164.32);
        priceCache.put("GBPJPY", 195.43);
        priceCache.put("EURAUD", 1.6543);
        priceCache.put("EURCAD", 1.4821);
        priceCache.put("AUDCAD", 0.9012);
        priceCache.put("AUDJPY", 101.23);
        priceCache.put("CADJPY", 112.43);
        priceCache.put("NZDJPY", 92.34);
        priceCache.put("GBPAUD", 1.9432);
        priceCache.put("GBPCAD", 1.7543);

        // Commodities
        priceCache.put("XAUUSD", 2341.80);
        priceCache.put("XAGUSD", 27.45);
        priceCache.put("USOIL", 78.54);
        priceCache.put("UKOIL", 82.34);

        // Indices (static - updated in Phase 6)
        priceCache.put("US30", 38543.00);
        priceCache.put("NAS100", 17832.00);
        priceCache.put("SPX500", 5123.00);
        priceCache.put("GER40", 17654.00);
        priceCache.put("UK100", 8123.00);
        priceCache.put("JPN225", 38432.00);

        // Crypto
        priceCache.put("BTCUSDT", 67432.50);
        priceCache.put("ETHUSDT", 3521.30);
        priceCache.put("BNBUSDT", 412.50);
        priceCache.put("SOLUSDT", 178.90);
        priceCache.put("XRPUSDT", 0.5432);
        priceCache.put("ADAUSDT", 0.4521);
        priceCache.put("DOGEUSDT", 0.1234);

        // Fetch real prices on startup
        fetchGoldAndSilver();
        fetchForexPrices();
        fetchCryptoPrices();
    }

    public Double getPrice(String symbol) {
        return priceCache.getOrDefault(symbol.toUpperCase(), 0.0);
    }

    public Map<String, Double> getAllPrices() {
        return new HashMap<>(priceCache);
    }

    public void updatePrice(String symbol, Double price) {
        priceCache.put(symbol.toUpperCase(), price);
    }

    // Fetch crypto prices from Binance (FREE - real time)
    public void fetchCryptoPrices() {
        List<String> symbols = List.of(
                "BTCUSDT", "ETHUSDT", "BNBUSDT", "SOLUSDT",
                "XRPUSDT", "ADAUSDT", "DOGEUSDT");

        for (String symbol : symbols) {
            binanceClient.get()
                    .uri("/api/v3/ticker/price?symbol=" + symbol)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .subscribe(response -> {
                        if (response != null && response.containsKey("price")) {
                            String sym = response.get("symbol").toString();
                            Double price = Double.parseDouble(
                                    response.get("price").toString());
                            priceCache.put(sym, price);
                            System.out.println("Binance: " + sym + " → " + price);
                        }
                    }, error -> System.err.println(
                            "Binance error: " + error.getMessage()));
        }
    }

    // Fetch forex prices from Frankfurter (FREE)
    public void fetchForexPrices() {
        // Fetch all major currencies vs USD
        frankfurterClient.get()
                .uri("/latest?from=USD&to=EUR,GBP,JPY,CHF,AUD,CAD,NZD")
                .retrieve()
                .bodyToMono(Map.class)
                .subscribe(response -> {
                    if (response != null && response.containsKey("rates")) {
                        Map<String, Object> rates =
                                (Map<String, Object>) response.get("rates");

                        // Calculate USD pairs
                        double eur = getRate(rates, "EUR");
                        double gbp = getRate(rates, "GBP");
                        double jpy = getRate(rates, "JPY");
                        double chf = getRate(rates, "CHF");
                        double aud = getRate(rates, "AUD");
                        double cad = getRate(rates, "CAD");
                        double nzd = getRate(rates, "NZD");

                        // Majors (USD pairs)
                        if (eur > 0) priceCache.put("EURUSD", round(1.0 / eur));
                        if (gbp > 0) priceCache.put("GBPUSD", round(1.0 / gbp));
                        if (jpy > 0) priceCache.put("USDJPY", round(jpy));
                        if (chf > 0) priceCache.put("USDCHF", round(chf));
                        if (aud > 0) priceCache.put("AUDUSD", round(1.0 / aud));
                        if (cad > 0) priceCache.put("USDCAD", round(cad));
                        if (nzd > 0) priceCache.put("NZDUSD", round(1.0 / nzd));

                        // Minors (cross pairs - calculated from USD rates)
                        if (eur > 0 && gbp > 0) priceCache.put("EURGBP", round(gbp / eur));
                        if (eur > 0 && jpy > 0) priceCache.put("EURJPY", round(jpy / eur));
                        if (gbp > 0 && jpy > 0) priceCache.put("GBPJPY", round(jpy / gbp));
                        if (eur > 0 && aud > 0) priceCache.put("EURAUD", round(aud / eur));
                        if (eur > 0 && cad > 0) priceCache.put("EURCAD", round(cad / eur));
                        if (aud > 0 && cad > 0) priceCache.put("AUDCAD", round(cad / aud));
                        if (aud > 0 && jpy > 0) priceCache.put("AUDJPY", round(jpy / aud));
                        if (cad > 0 && jpy > 0) priceCache.put("CADJPY", round(jpy / cad));
                        if (nzd > 0 && jpy > 0) priceCache.put("NZDJPY", round(jpy / nzd));
                        if (gbp > 0 && aud > 0) priceCache.put("GBPAUD", round(aud / gbp));
                        if (gbp > 0 && cad > 0) priceCache.put("GBPCAD", round(cad / gbp));

                        System.out.println("Frankfurter: Forex prices updated! ✅");
                    }
                }, error -> System.err.println(
                        "Frankfurter error: " + error.getMessage()));
    }

    // Fetch Gold and Silver prices (FREE)
    public void fetchGoldAndSilver() {
        // Gold
        goldClient.get()
                .uri("/price/XAU")
                .retrieve()
                .bodyToMono(Map.class)
                .subscribe(response -> {
                    if (response != null && response.containsKey("price")) {
                        Double price = Double.parseDouble(
                                response.get("price").toString());
                        priceCache.put("XAUUSD", price);
                        System.out.println("gold-api: XAUUSD → " + price);
                    }
                }, error -> System.err.println(
                        "gold-api error: " + error.getMessage()));

        // Silver
        goldClient.get()
                .uri("/price/XAG")
                .retrieve()
                .bodyToMono(Map.class)
                .subscribe(response -> {
                    if (response != null && response.containsKey("price")) {
                        Double price = Double.parseDouble(
                                response.get("price").toString());
                        priceCache.put("XAGUSD", price);
                        System.out.println("gold-api: XAGUSD → " + price);
                    }
                }, error -> System.err.println(
                        "gold-api silver error: " + error.getMessage()));
    }

    // Helper: get rate from map safely
    private double getRate(Map<String, Object> rates, String currency) {
        if (rates.containsKey(currency)) {
            return Double.parseDouble(rates.get(currency).toString());
        }
        return 0.0;
    }

    // Helper: round to 4 decimal places
    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}