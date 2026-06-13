package com.mvp18.trading_challenge_backend.service.interfaces;

import com.mvp18.trading_challenge_backend.dto.OpenTradeRequest;
import com.mvp18.trading_challenge_backend.dto.TradeResponse;
import java.util.List;

public interface ITradeService {
    TradeResponse openTrade(String email,
                            OpenTradeRequest request);
    TradeResponse closeTrade(Long tradeId, String email);
    List<TradeResponse> getOpenTrades(String email);
    List<TradeResponse> getAllTrades(String email);
}