package com.mvp18.trading_challenge_backend.service;

import com.mvp18.trading_challenge_backend.ChallengeAttempt;
import com.mvp18.trading_challenge_backend.ChallengeRules;
import com.mvp18.trading_challenge_backend.TradingAccount;
import com.mvp18.trading_challenge_backend.User;
import com.mvp18.trading_challenge_backend.repository.ChallengeAttemptRepository;
import com.mvp18.trading_challenge_backend.repository.ChallengeRulesRepository;
import com.mvp18.trading_challenge_backend.repository.TradingAccountRepository;
import com.mvp18.trading_challenge_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final UserRepository userRepository;
    private final ChallengeRulesRepository challengeRulesRepository;
    private final TradingAccountRepository tradingAccountRepository;
    private final ChallengeAttemptRepository challengeAttemptRepository;

    // Get all available challenge rules
    public List<ChallengeRules> getAllRules() {
        return challengeRulesRepository.findAll();
    }

    // Start a new challenge
    public Map<String, Object> startChallenge(String email, BigDecimal accountSize) {
        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find matching rules
        List<ChallengeRules> rules = challengeRulesRepository.findByAccountSize(accountSize);
        if (rules.isEmpty()) {
            throw new RuntimeException("No rules found for account size: " + accountSize);
        }
        ChallengeRules rule = rules.get(0);

        // Create trading account
        TradingAccount account = new TradingAccount();
        account.setUser(user);
        account.setAccountSize(accountSize);
        account.setStartingBalance(accountSize);
        account.setCurrentBalance(accountSize);
        account.setStatus("ACTIVE");
        tradingAccountRepository.save(account);

        // Create challenge attempt
        ChallengeAttempt attempt = new ChallengeAttempt();
        attempt.setUser(user);
        attempt.setTradingAccount(account);
        attempt.setChallengeRules(rule);
        attempt.setStatus("ACTIVE");
        challengeAttemptRepository.save(attempt);

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Challenge started successfully");
        response.put("challengeAttemptId", attempt.getId());
        response.put("tradingAccountId", account.getId());
        response.put("accountSize", accountSize);
        response.put("profitTarget", rule.getProfitTargetPercent() + "%");
        response.put("maxDrawdown", rule.getMaxDrawdownPercent() + "%");
        response.put("dailyLossLimit", rule.getDailyLossLimitPercent() + "%");
        response.put("maxTradingDays", rule.getMaxTradingDays());

        return response;
    }

    // Get user's active challenges
    public List<ChallengeAttempt> getUserChallenges(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return challengeAttemptRepository.findByUserId(user.getId());
    }
}