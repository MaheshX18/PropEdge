package com.mvp18.trading_challenge_backend.repository;

import com.mvp18.trading_challenge_backend.ChallengeRules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ChallengeRulesRepository extends JpaRepository<ChallengeRules, Long> {
    List<ChallengeRules> findByFirmName(String firmName);
    List<ChallengeRules> findByAccountSize(BigDecimal accountSize);
}