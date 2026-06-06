package com.mvp18.trading_challenge_backend.repository;

import com.mvp18.trading_challenge_backend.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByUserId(Long userId);
    List<Trade> findByUserIdAndStatus(Long userId, String status);
    List<Trade> findByTradingAccountId(Long tradingAccountId);
    List<Trade> findByTradingAccountIdAndStatus(Long tradingAccountId, String status);
    List<Trade> findByChallengeAttemptId(Long challengeAttemptId);
}