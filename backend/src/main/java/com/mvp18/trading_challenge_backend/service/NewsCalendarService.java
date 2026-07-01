package com.mvp18.trading_challenge_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NewsCalendarService {

    @Value("${finnhub.api.key}")
    private String finnhubApiKey;

    @Value("${finnhub.base.url}")
    private String finnhubBaseUrl;

    private WebClient finnhubClient;

    // Cache news events to avoid too many API calls
    // Key: date string, Value: list of news events
    private final ConcurrentHashMap<String, List<Map<String, Object>>>
            newsCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        finnhubClient = WebClient.builder()
                .baseUrl(finnhubBaseUrl)
                .build();
    }

    // Check if a trade violates news trading rules
    public NewsViolation checkNewsViolation(
            String symbol,
            long tradeTimeEpoch,
            int windowMinutes) {

        // Get affected currencies from symbol
        List<String> affectedCurrencies =
                getAffectedCurrencies(symbol);

        if (affectedCurrencies.isEmpty()) {
            return new NewsViolation(false, null);
        }

        // Get news events for today
        List<Map<String, Object>> events = getTodayNewsEvents();

        // Check if any high-impact news is within window
        Instant tradeTime = Instant.ofEpochSecond(tradeTimeEpoch);

        for (Map<String, Object> event : events) {
            // Only check high-impact events (impact = 3)
            Object impact = event.get("impact");
            if (impact == null) continue;

            int impactLevel = Integer.parseInt(impact.toString());
            if (impactLevel < 3) continue; // Only red/high impact

            // Check currency match
            String eventCurrency = (String) event.get("country");
            if (eventCurrency == null) continue;

            boolean currencyMatch = affectedCurrencies.stream()
                    .anyMatch(c -> c.equalsIgnoreCase(eventCurrency));

            if (!currencyMatch) continue;

            // Check time window
            Object eventTimeObj = event.get("time");
            if (eventTimeObj == null) continue;

            Instant eventTime = Instant.parse(
                    eventTimeObj.toString());
            long minutesDiff = Math.abs(ChronoUnit.MINUTES.between(
                    tradeTime, eventTime));

            if (minutesDiff <= windowMinutes) {
                String eventName = (String) event.getOrDefault(
                        "event", "High-impact news");
                return new NewsViolation(true,
                        "Trade placed within " + minutesDiff
                                + " minutes of high-impact news: "
                                + eventName + " (" + eventCurrency + ")");
            }
        }

        return new NewsViolation(false, null);
    }

    // Fetch today's economic calendar from Finnhub
    public List<Map<String, Object>> getTodayNewsEvents() {
        String today = java.time.LocalDate.now().toString();

        // Return cached if available
        if (newsCache.containsKey(today)) {
            return newsCache.get(today);
        }

        List<Map<String, Object>> events = new ArrayList<>();

        try {
            try {
                Map response = finnhubClient.get()
                        .uri("/calendar/economic?token=" + finnhubApiKey)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block(java.time.Duration.ofSeconds(5));

                if (response != null
                        && response.containsKey("economicCalendar")) {
                    List<Map<String, Object>> calendar =
                            (List<Map<String, Object>>)
                                    response.get("economicCalendar");
                    if (calendar != null) {
                        events.addAll(calendar);
                        newsCache.put(today, events);
                        System.out.println("Finnhub: Loaded "
                                + events.size() + " news events");
                    }
                }
            } catch (Exception e) {
                System.err.println("News calendar fetch failed: "
                        + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("News calendar fetch failed: "
                    + e.getMessage());
        }

        return events;
    }

    // Get currencies affected by a trading symbol
    private List<String> getAffectedCurrencies(String symbol) {
        List<String> currencies = new ArrayList<>();
        String sym = symbol.toUpperCase();

        // Forex pairs
        if (sym.length() == 6) {
            currencies.add(sym.substring(0, 3)); // Base currency
            currencies.add(sym.substring(3, 6)); // Quote currency
        }

        // Gold/Silver (affected by USD)
        if (sym.equals("XAUUSD") || sym.equals("XAGUSD")) {
            currencies.add("USD");
        }

        // Oil (affected by USD)
        if (sym.equals("USOIL") || sym.equals("UKOIL")) {
            currencies.add("USD");
        }

        // Crypto - not affected by forex news
        if (sym.endsWith("USDT")) {
            return new ArrayList<>();
        }

        return currencies;
    }

    // News violation resuls
    public static class NewsViolation {
        private final boolean violated;
        private final String reason;

        public NewsViolation(boolean violated, String reason) {
            this.violated = violated;
            this.reason = reason;
        }

        public boolean isViolated() { return violated; }
        public String getReason() { return reason; }
    }
}