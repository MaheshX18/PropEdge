package com.mvp18.trading_challenge_backend.service;

import com.mvp18.trading_challenge_backend.ChallengeAttempt;
import com.mvp18.trading_challenge_backend.Trade;
import com.mvp18.trading_challenge_backend.TradingAccount;
import com.mvp18.trading_challenge_backend.User;
import com.mvp18.trading_challenge_backend.repository.ChallengeAttemptRepository;
import com.mvp18.trading_challenge_backend.repository.TradeRepository;
import com.mvp18.trading_challenge_backend.repository.TradingAccountRepository;
import com.mvp18.trading_challenge_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;
    private final TradingAccountRepository tradingAccountRepository;
    private final ChallengeAttemptRepository challengeAttemptRepository;
    private final MarketDataService marketDataService;

    // Open a new trade
    public Map<String, Object> openTrade(String email,
                                         Long challengeAttemptId,
                                         String symbol,
                                         String direction,
                                         BigDecimal lotSize,
                                         BigDecimal stopLoss,
                                         BigDecimal takeProfit) {

        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find challenge attempt
        ChallengeAttempt attempt = challengeAttemptRepository
                .findById(challengeAttemptId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        // Check challenge is active
        if (!attempt.getStatus().equals("ACTIVE")) {
            throw new RuntimeException("Challenge is not active");
        }

        // Get current market price
        Double currentPrice = marketDataService.getPrice(symbol);
        if (currentPrice == 0.0) {
            throw new RuntimeException("Symbol not found: " + symbol);
        }

        // Create trade
        Trade trade = new Trade();
        trade.setUser(user);
        trade.setTradingAccount(attempt.getTradingAccount());
        trade.setChallengeAttempt(attempt);
        trade.setSymbol(symbol.toUpperCase());
        trade.setDirection(direction.toUpperCase());
        trade.setLotSize(lotSize);
        trade.setOpenPrice(BigDecimal.valueOf(currentPrice));
        trade.setStatus("OPEN");

        if (stopLoss != null) trade.setStopLoss(stopLoss);
        if (takeProfit != null) trade.setTakeProfit(takeProfit);

        tradeRepository.save(trade);

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Trade opened successfully");
        response.put("tradeId", trade.getId());
        response.put("symbol", symbol.toUpperCase());
        response.put("direction", direction.toUpperCase());
        response.put("lotSize", lotSize);
        response.put("openPrice", currentPrice);
        response.put("stopLoss", stopLoss);
        response.put("takeProfit", takeProfit);
        response.put("status", "OPEN");

        return response;
    }

    // Close an existing trade
    public Map<String, Object> closeTrade(Long tradeId, String email) {

        // Find trade
        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new RuntimeException("Trade not found"));

        // Verify trade belongs to user
        if (!trade.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }

        // Check trade is open
        if (!trade.getStatus().equals("OPEN")) {
            throw new RuntimeException("Trade is already closed");
        }

        // Get current market price
        Double currentPrice = marketDataService.getPrice(trade.getSymbol());
        BigDecimal closePrice = BigDecimal.valueOf(currentPrice);

        // Calculate P&L
        BigDecimal pnl = calculatePnL(trade, closePrice);

        // Update trade
        trade.setClosePrice(closePrice);
        trade.setProfitLoss(pnl);
        trade.setStatus("CLOSED");
        trade.setClosedAt(System.currentTimeMillis() / 1000);
        tradeRepository.save(trade);

        // Update trading account balance
        TradingAccount account = trade.getTradingAccount();
        BigDecimal newBalance = account.getCurrentBalance().add(pnl);
        account.setCurrentBalance(newBalance);
        tradingAccountRepository.save(account);

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Trade closed successfully");
        response.put("tradeId", tradeId);
        response.put("symbol", trade.getSymbol());
        response.put("direction", trade.getDirection());
        response.put("openPrice", trade.getOpenPrice());
        response.put("closePrice", closePrice);
        response.put("lotSize", trade.getLotSize());
        response.put("profitLoss", pnl);
        response.put("newBalance", newBalance);

        return response;
    }

    // Get all open trades for a user
    public List<Trade> getOpenTrades(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return tradeRepository.findByUserIdAndStatus(user.getId(), "OPEN");
    }

    // Get all trades for a user
    public List<Trade> getAllTrades(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return tradeRepository.findByUserId(user.getId());
    }

    // Calculate P&L
    private BigDecimal calculatePnL(Trade trade, BigDecimal closePrice) {
        BigDecimal openPrice = trade.getOpenPrice();
        BigDecimal lotSize = trade.getLotSize();
        BigDecimal priceDiff;
        String symbol = trade.getSymbol().toUpperCase();

        if (trade.getDirection().equals("BUY")) {
            priceDiff = closePrice.subtract(openPrice);
        } else {
            priceDiff = openPrice.subtract(closePrice);
        }

        // Contract size depends on instrument type
        BigDecimal contractSize;

        if (symbol.endsWith("USDT") || symbol.endsWith("BTC")
                || symbol.endsWith("ETH")) {
            // Crypto - no contract size multiplier
            contractSize = BigDecimal.ONE;
        } else if (symbol.equals("XAUUSD")) {
            // Gold - 100 oz per lot
            contractSize = BigDecimal.valueOf(100);
        } else if (symbol.equals("XAGUSD")) {
            // Silver - 5000 oz per lot
            contractSize = BigDecimal.valueOf(5000);
        } else if (symbol.equals("USOIL") || symbol.equals("UKOIL")) {
            // Oil - 1000 barrels per lot
            contractSize = BigDecimal.valueOf(1000);
        } else {
            // Forex - standard 100,000 units per lot
            contractSize = BigDecimal.valueOf(100000);
        }

        BigDecimal pnl = priceDiff
                .multiply(lotSize)
                .multiply(contractSize)
                .setScale(2, RoundingMode.HALF_UP);

        return pnl;
    }
}