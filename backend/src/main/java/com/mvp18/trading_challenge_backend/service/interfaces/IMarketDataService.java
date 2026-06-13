package com.mvp18.trading_challenge_backend.service.interfaces;

import java.util.Map;

public interface IMarketDataService {
    Double getPrice(String symbol);
    Map<String, Double> getAllPrices();
    void updatePrice(String symbol, Double price);
    void fetchCryptoPrices();
    void fetchForexPrices();
    void fetchGoldAndSilver();
}