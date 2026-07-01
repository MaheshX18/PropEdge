package com.mvp18.trading_challenge_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeResponse {
    private Long tradeId;
    private String symbol;
    private String direction;
    private BigDecimal lotSize;
    private BigDecimal openPrice;
    private BigDecimal closePrice;
    private BigDecimal stopLoss;
    private BigDecimal takeProfit;
    private BigDecimal profitLoss;
    private BigDecimal currentBalance;
    private String status;
    private Long openedAt;
    private Long closedAt;
    private String ruleCheckMessage;
    private String ruleViolationType;
    private String challengeStatus;
}