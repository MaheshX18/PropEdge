package com.mvp18.trading_challenge_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class PriceScheduler {

    private final MarketDataService marketDataService;

    // Fetch crypto prices every 5 seconds (Binance FREE)
    @Scheduled(fixedDelay = 5000)
    public void fetchCryptoPrices() {
        marketDataService.fetchCryptoPrices();
    }

    // Fetch forex prices every 30 seconds (Frankfurter FREE)
    @Scheduled(fixedDelay = 30000)
    public void fetchForexPrices() {
        marketDataService.fetchForexPrices();
    }

    // Fetch gold price every 60 seconds (Alpha Vantage FREE tier)
    @Scheduled(fixedDelay = 60000)
    public void fetchGoldPrice() {
        marketDataService.fetchGoldPrice();
    }
}