package com.mvp18.trading_challenge_backend.service;

import com.mvp18.trading_challenge_backend.ChallengeAttempt;
import com.mvp18.trading_challenge_backend.Trade;
import com.mvp18.trading_challenge_backend.TradingAccount;
import com.mvp18.trading_challenge_backend.User;
import com.mvp18.trading_challenge_backend.dto.OpenTradeRequest;
import com.mvp18.trading_challenge_backend.dto.TradeResponse;
import com.mvp18.trading_challenge_backend.exception.BusinessException;
import com.mvp18.trading_challenge_backend.exception.ResourceNotFoundException;
import com.mvp18.trading_challenge_backend.exception.UnauthorizedException;
import com.mvp18.trading_challenge_backend.repository.ChallengeAttemptRepository;
import com.mvp18.trading_challenge_backend.repository.TradeRepository;
import com.mvp18.trading_challenge_backend.repository.TradingAccountRepository;
import com.mvp18.trading_challenge_backend.repository.UserRepository;
import com.mvp18.trading_challenge_backend.service.interfaces.ITradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeService implements ITradeService {

    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;
    private final TradingAccountRepository tradingAccountRepository;
    private final ChallengeAttemptRepository challengeAttemptRepository;
    private final MarketDataService marketDataService;
    private final RuleEnforcementService ruleEnforcementService;

    @Override
    @Transactional
    public TradeResponse openTrade(String email,
                                   OpenTradeRequest request) {

        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));

        // Find challenge attempt
        ChallengeAttempt attempt = challengeAttemptRepository
                .findById(request.getChallengeAttemptId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Challenge attempt",
                        request.getChallengeAttemptId()));

        // Verify challenge belongs to user
        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException(
                    "Challenge does not belong to this user");
        }

        // Check challenge is active
        if (!attempt.getStatus().equals("ACTIVE")) {
            throw new BusinessException(
                    "Challenge is not active. Status: "
                            + attempt.getStatus());
        }

        // Get current market price
        Double currentPrice = marketDataService
                .getPrice(request.getSymbol().toUpperCase());
        if (currentPrice == 0.0) {
            throw new BusinessException(
                    "Symbol not found or price unavailable: "
                            + request.getSymbol());
        }

        // Create trade
        Trade trade = new Trade();
        trade.setUser(user);
        trade.setTradingAccount(attempt.getTradingAccount());
        trade.setChallengeAttempt(attempt);
        trade.setSymbol(request.getSymbol().toUpperCase());
        trade.setDirection(request.getDirection().toUpperCase());
        trade.setLotSize(request.getLotSize());
        trade.setOpenPrice(BigDecimal.valueOf(currentPrice));
        trade.setStatus("OPEN");
        trade.setStopLoss(request.getStopLoss());
        trade.setTakeProfit(request.getTakeProfit());

        tradeRepository.save(trade);

        return mapToTradeResponse(trade,
                attempt.getTradingAccount().getCurrentBalance());
    }

    @Override
    @Transactional
    public TradeResponse closeTrade(Long tradeId, String email) {

        // Find trade
        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trade", tradeId));

        // Verify trade belongs to user
        if (!trade.getUser().getEmail().equals(email)) {
            throw new UnauthorizedException(
                    "Trade does not belong to this user");
        }

        // Check trade is open
        if (!trade.getStatus().equals("OPEN")) {
            throw new BusinessException(
                    "Trade is already closed");
        }

        // Get current market price
        Double currentPrice = marketDataService
                .getPrice(trade.getSymbol());
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

        // Get challenge attempt from trade
        ChallengeAttempt attempt = trade.getChallengeAttempt();

        // Check rules after trade closes
        RuleEnforcementService.RuleCheckResult ruleResult =
                ruleEnforcementService.checkRulesAfterTrade(attempt, trade);

        TradeResponse tradeResponse = mapToTradeResponse(trade, newBalance);
        tradeResponse.setRuleCheckMessage(ruleResult.getMessage());
        tradeResponse.setRuleViolationType(ruleResult.getViolationType());
        tradeResponse.setChallengeStatus(attempt.getStatus());

        return tradeResponse;
    }

    @Override
    public List<TradeResponse> getOpenTrades(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));
        return tradeRepository
                .findByUserIdAndStatus(user.getId(), "OPEN")
                .stream()
                .map(t -> mapToTradeResponse(t,
                        t.getTradingAccount().getCurrentBalance()))
                .collect(Collectors.toList());
    }

    @Override
    public List<TradeResponse> getAllTrades(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));
        return tradeRepository.findByUserId(user.getId())
                .stream()
                .map(t -> mapToTradeResponse(t,
                        t.getTradingAccount().getCurrentBalance()))
                .collect(Collectors.toList());
    }

    // Map Trade entity to TradeResponse DTO
    private TradeResponse mapToTradeResponse(Trade trade,
                                             BigDecimal currentBalance) {
        TradeResponse response = new TradeResponse();
        response.setTradeId(trade.getId());
        response.setSymbol(trade.getSymbol());
        response.setDirection(trade.getDirection());
        response.setLotSize(trade.getLotSize());
        response.setOpenPrice(trade.getOpenPrice());
        response.setClosePrice(trade.getClosePrice());
        response.setStopLoss(trade.getStopLoss());
        response.setTakeProfit(trade.getTakeProfit());
        response.setProfitLoss(trade.getProfitLoss());
        response.setCurrentBalance(currentBalance);
        response.setStatus(trade.getStatus());
        response.setOpenedAt(trade.getOpenedAt());
        response.setClosedAt(trade.getClosedAt());
        return response;
    }

    // Calculate P&L based on instrument type
    private BigDecimal calculatePnL(Trade trade,
                                    BigDecimal closePrice) {
        BigDecimal openPrice = trade.getOpenPrice();
        BigDecimal lotSize = trade.getLotSize();
        BigDecimal priceDiff;
        String symbol = trade.getSymbol().toUpperCase();

        if (trade.getDirection().equals("BUY")) {
            priceDiff = closePrice.subtract(openPrice);
        } else {
            priceDiff = openPrice.subtract(closePrice);
        }

        BigDecimal contractSize;
        if (symbol.endsWith("USDT") || symbol.endsWith("BTC")
                || symbol.endsWith("ETH")) {
            contractSize = BigDecimal.ONE;
        } else if (symbol.equals("XAUUSD")) {
            contractSize = BigDecimal.valueOf(100);
        } else if (symbol.equals("XAGUSD")) {
            contractSize = BigDecimal.valueOf(5000);
        } else if (symbol.equals("USOIL")
                || symbol.equals("UKOIL")) {
            contractSize = BigDecimal.valueOf(1000);
        } else {
            contractSize = BigDecimal.valueOf(100000);
        }

        return priceDiff
                .multiply(lotSize)
                .multiply(contractSize)
                .setScale(2, RoundingMode.HALF_UP);
    }
}