package com.mvp18.trading_challenge_backend.controller;

import com.mvp18.trading_challenge_backend.dto.ApiResponse;
import com.mvp18.trading_challenge_backend.dto.OpenTradeRequest;
import com.mvp18.trading_challenge_backend.dto.TradeResponse;
import com.mvp18.trading_challenge_backend.security.SecurityUtils;
import com.mvp18.trading_challenge_backend.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/trade")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    // POST /api/trade/open
    @PostMapping("/open")
    public ResponseEntity<ApiResponse<TradeResponse>> openTrade(
            @Valid @RequestBody OpenTradeRequest request) {
        // Email extracted from JWT token automatically!
        String email = SecurityUtils.getCurrentUserEmail();
        TradeResponse response = tradeService.openTrade(email, request);
        return ResponseEntity.ok(
                ApiResponse.success("Trade opened successfully", response));
    }

    // POST /api/trade/close
    @PostMapping("/close")
    public ResponseEntity<ApiResponse<TradeResponse>> closeTrade(
            @RequestParam Long tradeId) {
        // Email extracted from JWT token automatically!
        String email = SecurityUtils.getCurrentUserEmail();
        TradeResponse response = tradeService.closeTrade(tradeId, email);
        return ResponseEntity.ok(
                ApiResponse.success("Trade closed successfully", response));
    }

    // GET /api/trade/open-trades
    @GetMapping("/open-trades")
    public ResponseEntity<ApiResponse<List<TradeResponse>>> getOpenTrades() {
        String email = SecurityUtils.getCurrentUserEmail();
        List<TradeResponse> trades = tradeService.getOpenTrades(email);
        return ResponseEntity.ok(ApiResponse.success(trades));
    }

    // GET /api/trade/all-trades
    @GetMapping("/all-trades")
    public ResponseEntity<ApiResponse<List<TradeResponse>>> getAllTrades() {
        String email = SecurityUtils.getCurrentUserEmail();
        List<TradeResponse> trades = tradeService.getAllTrades(email);
        return ResponseEntity.ok(ApiResponse.success(trades));
    }
}