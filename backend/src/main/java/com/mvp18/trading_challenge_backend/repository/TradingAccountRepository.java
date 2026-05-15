package com.mvp18.trading_challenge_backend.repository;

import com.mvp18.trading_challenge_backend.TradingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TradingAccountRepository extends JpaRepository<TradingAccount, Long> {
    List<TradingAccount> findByUserId(Long userId);
    List<TradingAccount> findByUserIdAndStatus(Long userId, String status);
}