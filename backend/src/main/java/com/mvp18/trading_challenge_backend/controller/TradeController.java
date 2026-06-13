package com.mvp18.trading_challenge_backend.controller;

import com.mvp18.trading_challenge_backend.dto.ApiResponse;
import com.mvp18.trading_challenge_backend.dto.OpenTradeRequest;
import com.mvp18.trading_challenge_backend.dto.TradeResponse;
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

    @PostMapping("/open")
    public ResponseEntity<ApiResponse<TradeResponse>> openTrade(
            @RequestParam String email,
            @Valid @RequestBody OpenTradeRequest request) {
        TradeResponse response = tradeService.openTrade(email, request);
        return ResponseEntity.ok(
                ApiResponse.success("Trade opened successfully", response));
    }

    @PostMapping("/close")
    public ResponseEntity<ApiResponse<TradeResponse>> closeTrade(
            @RequestParam String email,
            @RequestParam Long tradeId) {
        TradeResponse response = tradeService.closeTrade(tradeId, email);
        return ResponseEntity.ok(
                ApiResponse.success("Trade closed successfully", response));
    }

    @GetMapping("/open-trades")
    public ResponseEntity<ApiResponse<List<TradeResponse>>> getOpenTrades(
            @RequestParam String email) {
        List<TradeResponse> trades = tradeService.getOpenTrades(email);
        return ResponseEntity.ok(ApiResponse.success(trades));
    }

    @GetMapping("/all-trades")
    public ResponseEntity<ApiResponse<List<TradeResponse>>> getAllTrades(
            @RequestParam String email) {
        List<TradeResponse> trades = tradeService.getAllTrades(email);
        return ResponseEntity.ok(ApiResponse.success(trades));
    }
}