package com.mvp18.trading_challenge_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeResponse {
    private Long challengeAttemptId;
    private Long tradingAccountId;
    private String firmName;
    private String challengeType;
    private BigDecimal accountSize;
    private BigDecimal currentBalance;
    private BigDecimal startingBalance;
    private BigDecimal profitTarget;
    private BigDecimal maxDrawdown;
    private BigDecimal dailyLossLimit;
    private String status;
    private Long startedAt;
}