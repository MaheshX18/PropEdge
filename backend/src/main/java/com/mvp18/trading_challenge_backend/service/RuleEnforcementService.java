package com.mvp18.trading_challenge_backend.service;

import com.mvp18.trading_challenge_backend.ChallengeAttempt;
import com.mvp18.trading_challenge_backend.ChallengeRules;
import com.mvp18.trading_challenge_backend.Trade;
import com.mvp18.trading_challenge_backend.TradingAccount;
import com.mvp18.trading_challenge_backend.repository.ChallengeAttemptRepository;
import com.mvp18.trading_challenge_backend.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RuleEnforcementService {

    private final ChallengeAttemptRepository challengeAttemptRepository;
    private final TradeRepository tradeRepository;
    private final NewsCalendarService newsCalendarService;

    // Main method - called after every trade closes
    @Transactional
    public RuleCheckResult checkRulesAfterTrade(
            ChallengeAttempt attempt, Trade trade) {

        ChallengeRules rules = attempt.getChallengeRules();
        TradingAccount account = attempt.getTradingAccount();

        // 1. Check news trading violation
        if (rules.getNewsTradingAllowed() != null
                && !rules.getNewsTradingAllowed()) {
            int window = rules.getNewsTradingWindowMinutes() != null
                    ? rules.getNewsTradingWindowMinutes() : 5;
            NewsCalendarService.NewsViolation newsViolation =
                    newsCalendarService.checkNewsViolation(
                            trade.getSymbol(),
                            trade.getOpenedAt(),
                            window);
            if (newsViolation.isViolated()) {
                return failChallenge(attempt,
                        "NEWS_TRADING_VIOLATION",
                        newsViolation.getReason());
            }
        }

        // 2. Check daily loss limit
        RuleCheckResult dailyLossResult =
                checkDailyLossLimit(attempt, rules, account);
        if (!dailyLossResult.isPassed()) return dailyLossResult;

        // 3. Check max drawdown
        RuleCheckResult drawdownResult =
                checkMaxDrawdown(attempt, rules, account);
        if (!drawdownResult.isPassed()) return drawdownResult;

        // 4. Check floating loss limit
        if (rules.getFloatingLossLimitPercent() != null) {
            RuleCheckResult floatingResult =
                    checkFloatingLoss(attempt, rules, account);
            if (!floatingResult.isPassed()) return floatingResult;
        }

        // 5. Check if profit target reached
        RuleCheckResult profitResult =
                checkProfitTarget(attempt, rules, account);
        if (profitResult.isPassed() && profitResult.isTargetReached()) {
            return profitResult;
        }

        // 6. Check consistency rule
        if (rules.getConsistencyRulePercent() != null) {
            RuleCheckResult consistencyResult =
                    checkConsistencyRule(attempt, rules);
            if (!consistencyResult.isPassed()) return consistencyResult;
        }

        // 7. Check best day rule
        if (rules.getBestDayRulePercent() != null) {
            RuleCheckResult bestDayResult =
                    checkBestDayRule(attempt, rules);
            if (!bestDayResult.isPassed()) return bestDayResult;
        }

        return RuleCheckResult.passed("All rules satisfied");
    }

    // Check daily loss limit
    private RuleCheckResult checkDailyLossLimit(
            ChallengeAttempt attempt,
            ChallengeRules rules,
            TradingAccount account) {

        BigDecimal dailyLossPercent = rules.getDailyLossLimitPercent();
        if (dailyLossPercent == null || dailyLossPercent.compareTo(
                BigDecimal.ZERO) == 0) {
            return RuleCheckResult.passed("No daily loss limit");
        }

        // Get today's trades
        List<Trade> todayTrades = getTodayClosedTrades(
                attempt.getId());

        // Calculate today's P&L
        BigDecimal todayPnL = todayTrades.stream()
                .map(t -> t.getProfitLoss() != null
                        ? t.getProfitLoss() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate daily loss limit amount
        BigDecimal startingBalance = account.getStartingBalance();
        BigDecimal dailyLossAmount = startingBalance
                .multiply(dailyLossPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Check if daily loss exceeded
        if (todayPnL.negate().compareTo(dailyLossAmount) >= 0) {
            return failChallenge(attempt,
                    "DAILY_LOSS_LIMIT",
                    "Daily loss limit breached! Lost: $"
                            + todayPnL.negate()
                            + " | Limit: $" + dailyLossAmount);
        }

        return RuleCheckResult.passed("Daily loss within limits");
    }

    // Check max drawdown (static or trailing)
    private RuleCheckResult checkMaxDrawdown(
            ChallengeAttempt attempt,
            ChallengeRules rules,
            TradingAccount account) {

        BigDecimal maxDrawdownPercent = rules.getMaxDrawdownPercent();
        if (maxDrawdownPercent == null) {
            return RuleCheckResult.passed("No max drawdown rule");
        }

        BigDecimal currentBalance = account.getCurrentBalance();
        BigDecimal startingBalance = account.getStartingBalance();
        String maxLossType = rules.getMaxLossType();

        if ("TRAILING".equalsIgnoreCase(maxLossType)) {
            // Trailing drawdown - based on highest balance ever
            BigDecimal highestBalance = getHighestBalance(attempt.getId(),
                    startingBalance);
            BigDecimal drawdownAmount = highestBalance
                    .multiply(maxDrawdownPercent)
                    .divide(BigDecimal.valueOf(100), 2,
                            RoundingMode.HALF_UP);
            BigDecimal floor = highestBalance.subtract(drawdownAmount);

            if (currentBalance.compareTo(floor) <= 0) {
                return failChallenge(attempt,
                        "MAX_DRAWDOWN_TRAILING",
                        "Trailing drawdown breached! Balance: $"
                                + currentBalance
                                + " | Floor: $" + floor);
            }
        } else {
            // Static drawdown - based on initial balance
            BigDecimal drawdownAmount = startingBalance
                    .multiply(maxDrawdownPercent)
                    .divide(BigDecimal.valueOf(100), 2,
                            RoundingMode.HALF_UP);
            BigDecimal floor = startingBalance.subtract(drawdownAmount);

            if (currentBalance.compareTo(floor) <= 0) {
                return failChallenge(attempt,
                        "MAX_DRAWDOWN_STATIC",
                        "Max drawdown breached! Balance: $"
                                + currentBalance
                                + " | Floor: $" + floor);
            }
        }

        return RuleCheckResult.passed("Drawdown within limits");
    }

    // Check floating loss limit
    private RuleCheckResult checkFloatingLoss(
            ChallengeAttempt attempt,
            ChallengeRules rules,
            TradingAccount account) {

        BigDecimal floatingLimitPercent =
                rules.getFloatingLossLimitPercent();
        BigDecimal floatingLimitAmount = account.getStartingBalance()
                .multiply(floatingLimitPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Get open trades P&L
        List<Trade> openTrades = tradeRepository
                .findByTradingAccountIdAndStatus(
                        account.getId(), "OPEN");

        BigDecimal totalFloatingLoss = openTrades.stream()
                .filter(t -> t.getProfitLoss() != null
                        && t.getProfitLoss().compareTo(
                        BigDecimal.ZERO) < 0)
                .map(Trade::getProfitLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalFloatingLoss.negate().compareTo(
                floatingLimitAmount) >= 0) {
            return failChallenge(attempt,
                    "FLOATING_LOSS_LIMIT",
                    "Floating loss limit breached! Floating loss: $"
                            + totalFloatingLoss.negate()
                            + " | Limit: $" + floatingLimitAmount);
        }

        return RuleCheckResult.passed("Floating loss within limits");
    }

    // Check profit target
    private RuleCheckResult checkProfitTarget(
            ChallengeAttempt attempt,
            ChallengeRules rules,
            TradingAccount account) {

        BigDecimal profitTarget = rules.getProfitTargetPercent();
        if (profitTarget == null || profitTarget.compareTo(
                BigDecimal.ZERO) == 0) {
            return RuleCheckResult.passed("No profit target");
        }

        BigDecimal startingBalance = account.getStartingBalance();
        BigDecimal currentBalance = account.getCurrentBalance();
        BigDecimal targetAmount = startingBalance
                .multiply(profitTarget)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal targetBalance = startingBalance.add(targetAmount);

        // Check min trading days
        int tradingDaysCompleted = countValidTradingDays(attempt.getId(),
                rules.getMinProfitPerTradingDayPercent(),
                startingBalance);

        int minTradingDays = rules.getMinTradingDays() != null
                ? rules.getMinTradingDays() : 0;

        if (currentBalance.compareTo(targetBalance) >= 0) {
            if (tradingDaysCompleted >= minTradingDays) {
                // PASSED!
                return passChallenge(attempt,
                        "Profit target reached! Balance: $"
                                + currentBalance
                                + " | Target: $" + targetBalance);
            } else {
                return RuleCheckResult.passed(
                        "Profit target reached but need more trading days: "
                                + tradingDaysCompleted + "/"
                                + minTradingDays);
            }
        }

        return RuleCheckResult.passed("Profit target not yet reached");
    }

    // Check consistency rule
    private RuleCheckResult checkConsistencyRule(
            ChallengeAttempt attempt, ChallengeRules rules) {

        BigDecimal consistencyPercent =
                rules.getConsistencyRulePercent();

        List<Trade> closedTrades = tradeRepository
                .findByChallengeAttemptId(attempt.getId())
                .stream()
                .filter(t -> "CLOSED".equals(t.getStatus())
                        && t.getProfitLoss() != null
                        && t.getProfitLoss().compareTo(
                        BigDecimal.ZERO) > 0)
                .toList();

        if (closedTrades.isEmpty()) {
            return RuleCheckResult.passed("No profitable trades yet");
        }

        BigDecimal totalProfit = closedTrades.stream()
                .map(Trade::getProfitLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal bestDayProfit = getBestDayProfit(
                attempt.getId());

        if (totalProfit.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal bestDayPercent = bestDayProfit
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalProfit, 2, RoundingMode.HALF_UP);

            if (bestDayPercent.compareTo(consistencyPercent) > 0) {
                return RuleCheckResult.warning(
                        "Consistency rule: Best day is "
                                + bestDayPercent + "% of total profit. "
                                + "Limit: " + consistencyPercent + "%."
                                + " Keep trading to balance this out.");
            }
        }

        return RuleCheckResult.passed("Consistency rule satisfied");
    }

    // Check best day rule
    private RuleCheckResult checkBestDayRule(
            ChallengeAttempt attempt, ChallengeRules rules) {

        BigDecimal bestDayLimit = rules.getBestDayRulePercent();
        BigDecimal totalProfit = getTotalProfit(attempt.getId());
        BigDecimal bestDayProfit = getBestDayProfit(attempt.getId());

        if (totalProfit.compareTo(BigDecimal.ZERO) <= 0) {
            return RuleCheckResult.passed("No profit yet");
        }

        BigDecimal bestDayPercent = bestDayProfit
                .multiply(BigDecimal.valueOf(100))
                .divide(totalProfit, 2, RoundingMode.HALF_UP);

        if (bestDayPercent.compareTo(bestDayLimit) > 0) {
            return RuleCheckResult.warning(
                    "Best day rule: Your best day ("
                            + bestDayPercent + "%) exceeds "
                            + bestDayLimit + "% limit. "
                            + "Keep trading to balance this out.");
        }

        return RuleCheckResult.passed("Best day rule satisfied");
    }

    // Helper: Fail challenge
    private RuleCheckResult failChallenge(
            ChallengeAttempt attempt,
            String reason, String message) {
        attempt.setStatus("FAILED");
        attempt.setEndedAt(System.currentTimeMillis() / 1000);
        challengeAttemptRepository.save(attempt);
        return RuleCheckResult.failed(reason, message);
    }

    // Helper: Pass challenge
    private RuleCheckResult passChallenge(
            ChallengeAttempt attempt, String message) {
        attempt.setStatus("PASSED");
        attempt.setEndedAt(System.currentTimeMillis() / 1000);
        challengeAttemptRepository.save(attempt);
        return RuleCheckResult.targetReached(message);
    }

    // Helper: Get today's closed trades
    private List<Trade> getTodayClosedTrades(Long challengeAttemptId) {
        long startOfDay = Instant.now()
                .truncatedTo(ChronoUnit.DAYS)
                .getEpochSecond();
        return tradeRepository
                .findByChallengeAttemptId(challengeAttemptId)
                .stream()
                .filter(t -> "CLOSED".equals(t.getStatus())
                        && t.getClosedAt() != null
                        && t.getClosedAt() >= startOfDay)
                .toList();
    }

    // Helper: Get highest balance ever
    private BigDecimal getHighestBalance(
            Long challengeAttemptId, BigDecimal startingBalance) {
        List<Trade> allTrades = tradeRepository
                .findByChallengeAttemptId(challengeAttemptId);

        BigDecimal highest = startingBalance;
        BigDecimal running = startingBalance;

        for (Trade trade : allTrades) {
            if ("CLOSED".equals(trade.getStatus())
                    && trade.getProfitLoss() != null) {
                running = running.add(trade.getProfitLoss());
                if (running.compareTo(highest) > 0) {
                    highest = running;
                }
            }
        }
        return highest;
    }

    // Helper: Count valid trading days
    private int countValidTradingDays(
            Long challengeAttemptId,
            BigDecimal minProfitPercent,
            BigDecimal startingBalance) {

        List<Trade> closedTrades = tradeRepository
                .findByChallengeAttemptId(challengeAttemptId)
                .stream()
                .filter(t -> "CLOSED".equals(t.getStatus()))
                .toList();

        Map<String, BigDecimal> dailyPnL = new HashMap<>();

        for (Trade trade : closedTrades) {
            if (trade.getClosedAt() == null
                    || trade.getProfitLoss() == null) continue;
            String day = Instant.ofEpochSecond(trade.getClosedAt())
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()
                    .toString();
            dailyPnL.merge(day, trade.getProfitLoss(),
                    BigDecimal::add);
        }

        if (minProfitPercent == null) {
            return dailyPnL.size();
        }

        BigDecimal minAmount = startingBalance
                .multiply(minProfitPercent)
                .divide(BigDecimal.valueOf(100), 2,
                        RoundingMode.HALF_UP);

        return (int) dailyPnL.values().stream()
                .filter(pnl -> pnl.compareTo(minAmount) >= 0)
                .count();
    }

    // Helper: Get best day profit
    private BigDecimal getBestDayProfit(Long challengeAttemptId) {
        List<Trade> closedTrades = tradeRepository
                .findByChallengeAttemptId(challengeAttemptId)
                .stream()
                .filter(t -> "CLOSED".equals(t.getStatus()))
                .toList();

        Map<String, BigDecimal> dailyPnL = new HashMap<>();

        for (Trade trade : closedTrades) {
            if (trade.getClosedAt() == null
                    || trade.getProfitLoss() == null) continue;
            String day = Instant.ofEpochSecond(trade.getClosedAt())
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()
                    .toString();
            dailyPnL.merge(day, trade.getProfitLoss(),
                    BigDecimal::add);
        }

        return dailyPnL.values().stream()
                .filter(pnl -> pnl.compareTo(BigDecimal.ZERO) > 0)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    // Helper: Get total profit
    private BigDecimal getTotalProfit(Long challengeAttemptId) {
        return tradeRepository
                .findByChallengeAttemptId(challengeAttemptId)
                .stream()
                .filter(t -> "CLOSED".equals(t.getStatus())
                        && t.getProfitLoss() != null
                        && t.getProfitLoss().compareTo(
                        BigDecimal.ZERO) > 0)
                .map(Trade::getProfitLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Rule check result class
    public static class RuleCheckResult {
        private final boolean passed;
        private final boolean targetReached;
        private final String violationType;
        private final String message;
        private final boolean warning;

        private RuleCheckResult(boolean passed, boolean targetReached,
                                String violationType, String message, boolean warning) {
            this.passed = passed;
            this.targetReached = targetReached;
            this.violationType = violationType;
            this.message = message;
            this.warning = warning;
        }

        public static RuleCheckResult passed(String message) {
            return new RuleCheckResult(true, false,
                    null, message, false);
        }

        public static RuleCheckResult failed(
                String violationType, String message) {
            return new RuleCheckResult(false, false,
                    violationType, message, false);
        }

        public static RuleCheckResult targetReached(String message) {
            return new RuleCheckResult(true, true,
                    "PROFIT_TARGET", message, false);
        }

        public static RuleCheckResult warning(String message) {
            return new RuleCheckResult(true, false,
                    "WARNING", message, true);
        }

        public boolean isPassed() { return passed; }
        public boolean isTargetReached() { return targetReached; }
        public String getViolationType() { return violationType; }
        public String getMessage() { return message; }
        public boolean isWarning() { return warning; }
    }
}