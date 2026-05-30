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

    // Crypto every 5 seconds (Binance FREE)
    @Scheduled(fixedDelay = 5000)
    public void fetchCryptoPrices() {
        marketDataService.fetchCryptoPrices();
    }

    // Forex every 30 seconds (Frankfurter FREE)
    @Scheduled(fixedDelay = 30000)
    public void fetchForexPrices() {
        marketDataService.fetchForexPrices();
    }

    // Gold + Silver every 60 seconds (gold-api FREE)
    @Scheduled(fixedDelay = 60000)
    public void fetchMetalPrices() {
        marketDataService.fetchGoldAndSilver();
    }
}