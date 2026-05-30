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

    @Value("${alphavantage.api.key}")
    private String alphaVantageApiKey;

    @Value("${alphavantage.base.url}")
    private String alphaVantageBaseUrl;

    private final ConcurrentHashMap<String, Double> priceCache
            = new ConcurrentHashMap<>();

    private WebClient binanceClient;
    private WebClient frankfurterClient;

    @PostConstruct
    public void init() {
        binanceClient = WebClient.builder()
                .baseUrl(binanceBaseUrl).build();

        frankfurterClient = WebClient.builder()
                .baseUrl("https://api.frankfurter.app")
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().followRedirect(true)))
                .build();

        // Default prices
        priceCache.put("EURUSD", 1.0854);
        priceCache.put("GBPUSD", 1.2734);
        priceCache.put("USDJPY", 154.32);
        priceCache.put("USDCHF", 0.9012);
        priceCache.put("AUDUSD", 0.6543);
        priceCache.put("USDCAD", 1.3654);
        priceCache.put("XAUUSD", 2341.80);
        priceCache.put("XAGUSD", 27.45);
        priceCache.put("BTCUSDT", 67432.50);
        priceCache.put("ETHUSDT", 3521.30);
        priceCache.put("BNBUSDT", 412.50);
        priceCache.put("SOLUSDT", 178.90);

        // Fetch gold immediately on startup
        fetchGoldPrice();
    }

    public Double getPrice(String symbol) {
        return priceCache.getOrDefault(symbol, 0.0);
    }

    public Map<String, Double> getAllPrices() {
        return new HashMap<>(priceCache);
    }

    public void updatePrice(String symbol, Double price) {
        priceCache.put(symbol, price);
    }

    // Fetch crypto prices from Binance (FREE)
    public void fetchCryptoPrices() {
        List<String> symbols = List.of(
                "BTCUSDT", "ETHUSDT", "BNBUSDT", "SOLUSDT");

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
                            System.out.println("Binance: " + sym
                                    + " → " + price);
                        }
                    }, error -> System.err.println(
                            "Binance error: " + error.getMessage()));
        }
    }

    // Fetch forex prices from Frankfurter (FREE)
    public void fetchForexPrices() {
        frankfurterClient.get()
                .uri("/latest?from=USD&to=EUR,GBP,JPY,CHF,AUD,CAD")
                .retrieve()
                .bodyToMono(Map.class)
                .subscribe(response -> {
                    if (response != null && response.containsKey("rates")) {
                        Map<String, Object> rates =
                                (Map<String, Object>) response.get("rates");

                        if (rates.containsKey("EUR")) {
                            double eurUsd = 1.0 / Double.parseDouble(
                                    rates.get("EUR").toString());
                            priceCache.put("EURUSD",
                                    Math.round(eurUsd * 10000.0) / 10000.0);
                        }
                        if (rates.containsKey("GBP")) {
                            double gbpUsd = 1.0 / Double.parseDouble(
                                    rates.get("GBP").toString());
                            priceCache.put("GBPUSD",
                                    Math.round(gbpUsd * 10000.0) / 10000.0);
                        }
                        if (rates.containsKey("JPY")) {
                            priceCache.put("USDJPY", Double.parseDouble(
                                    rates.get("JPY").toString()));
                        }
                        if (rates.containsKey("CHF")) {
                            priceCache.put("USDCHF", Double.parseDouble(
                                    rates.get("CHF").toString()));
                        }
                        if (rates.containsKey("AUD")) {
                            double audUsd = 1.0 / Double.parseDouble(
                                    rates.get("AUD").toString());
                            priceCache.put("AUDUSD",
                                    Math.round(audUsd * 10000.0) / 10000.0);
                        }
                        if (rates.containsKey("CAD")) {
                            priceCache.put("USDCAD", Double.parseDouble(
                                    rates.get("CAD").toString()));
                        }
                        System.out.println("Frankfurter: Forex prices updated!");
                    }
                }, error -> System.err.println(
                        "Frankfurter error: " + error.getMessage()));
    }

    // Fetch Gold price from metals.live (FREE, no API key needed)
    // Fetch Gold price from gold-api.com (FREE, no API key needed)
    public void fetchGoldPrice() {
        System.out.println("Fetching gold price from gold-api.com...");
        WebClient goldClient = WebClient.builder()
                .baseUrl("https://api.gold-api.com")
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().followRedirect(true)))
                .build();

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
                    } else {
                        System.out.println("gold-api response: " + response);
                    }
                }, error -> System.err.println(
                        "gold-api error: " + error.getMessage()));
    }
}