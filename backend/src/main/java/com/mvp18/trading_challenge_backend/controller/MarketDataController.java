package com.mvp18.trading_challenge_backend.controller;

import com.mvp18.trading_challenge_backend.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
public class MarketDataController {

    private final MarketDataService marketDataService;

    // GET /api/market/prices - get all prices
    @GetMapping("/prices")
    public ResponseEntity<Map<String, Double>> getAllPrices() {
        return ResponseEntity.ok(marketDataService.getAllPrices());
    }

    // GET /api/market/test-gold - manually trigger gold fetch
    @GetMapping("/test-gold")
    public ResponseEntity<String> testGold() {
        marketDataService.fetchGoldPrice();
        return ResponseEntity.ok("Gold fetch triggered! Check terminal logs.");
    }

    // GET /api/market/price/{symbol} - get single price
    @GetMapping("/price/{symbol}")
    public ResponseEntity<Map<String, Object>> getPrice(
            @PathVariable String symbol) {
        Double price = marketDataService.getPrice(symbol.toUpperCase());
        return ResponseEntity.ok(Map.of(
                "symbol", symbol.toUpperCase(),
                "price", price
        ));
    }
}