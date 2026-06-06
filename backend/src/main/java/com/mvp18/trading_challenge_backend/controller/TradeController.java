package com.mvp18.trading_challenge_backend.controller;

import com.mvp18.trading_challenge_backend.Trade;
import com.mvp18.trading_challenge_backend.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/trade")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    // POST /api/trade/open
    @PostMapping("/open")
    public ResponseEntity<Map<String, Object>> openTrade(
            @RequestBody Map<String, Object> request) {

        String email = (String) request.get("email");
        Long challengeAttemptId = Long.parseLong(
                request.get("challengeAttemptId").toString());
        String symbol = (String) request.get("symbol");
        String direction = (String) request.get("direction");
        BigDecimal lotSize = new BigDecimal(
                request.get("lotSize").toString());

        BigDecimal stopLoss = request.get("stopLoss") != null
                ? new BigDecimal(request.get("stopLoss").toString()) : null;
        BigDecimal takeProfit = request.get("takeProfit") != null
                ? new BigDecimal(request.get("takeProfit").toString()) : null;

        return ResponseEntity.ok(tradeService.openTrade(
                email, challengeAttemptId, symbol,
                direction, lotSize, stopLoss, takeProfit));
    }

    // POST /api/trade/close
    @PostMapping("/close")
    public ResponseEntity<Map<String, Object>> closeTrade(
            @RequestBody Map<String, Object> request) {

        Long tradeId = Long.parseLong(request.get("tradeId").toString());
        String email = (String) request.get("email");

        return ResponseEntity.ok(tradeService.closeTrade(tradeId, email));
    }

    // GET /api/trade/open-trades?email=xxx
    @GetMapping("/open-trades")
    public ResponseEntity<List<Trade>> getOpenTrades(
            @RequestParam String email) {
        return ResponseEntity.ok(tradeService.getOpenTrades(email));
    }

    // GET /api/trade/all-trades?email=xxx
    @GetMapping("/all-trades")
    public ResponseEntity<List<Trade>> getAllTrades(
            @RequestParam String email) {
        return ResponseEntity.ok(tradeService.getAllTrades(email));
    }
}