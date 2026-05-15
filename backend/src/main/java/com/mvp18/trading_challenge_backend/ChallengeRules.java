package com.mvp18.trading_challenge_backend;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "challenge_rules")
public class ChallengeRules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firm_name", nullable = false)
    private String firmName;

    @Column(name = "account_size", nullable = false)
    private BigDecimal accountSize;

    @Column(name = "profit_target_percent", nullable = false)
    private BigDecimal profitTargetPercent;

    @Column(name = "max_drawdown_percent", nullable = false)
    private BigDecimal maxDrawdownPercent;

    @Column(name = "daily_loss_limit_percent", nullable = false)
    private BigDecimal dailyLossLimitPercent;

    @Column(name = "max_trading_days", nullable = false)
    private Integer maxTradingDays;

    @Column(name = "min_trading_days")
    private Integer minTradingDays;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis() / 1000;
    }
}