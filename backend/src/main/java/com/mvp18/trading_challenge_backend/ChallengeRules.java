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

    @Column(name = "challenge_type", nullable = false)
    private String challengeType;

    @Column(name = "phase_number", nullable = false)
    private Integer phaseNumber;

    @Column(name = "phase_count", nullable = false)
    private Integer phaseCount;

    @Column(name = "account_size", nullable = false)
    private BigDecimal accountSize;

    @Column(name = "profit_target_percent", nullable = false)
    private BigDecimal profitTargetPercent;

    @Column(name = "profit_target_phase2_percent")
    private BigDecimal profitTargetPhase2Percent;

    @Column(name = "profit_target_phase3_percent")
    private BigDecimal profitTargetPhase3Percent;

    @Column(name = "max_drawdown_percent", nullable = false)
    private BigDecimal maxDrawdownPercent;

    @Column(name = "daily_loss_limit_percent", nullable = false)
    private BigDecimal dailyLossLimitPercent;

    @Column(name = "max_loss_type")
    private String maxLossType;

    @Column(name = "daily_loss_type")
    private String dailyLossType;

    @Column(name = "trailing_daily_loss_percent")
    private BigDecimal trailingDailyLossPercent;

    @Column(name = "trailing_max_loss_percent")
    private BigDecimal trailingMaxLossPercent;

    @Column(name = "max_trading_days")
    private Integer maxTradingDays;

    @Column(name = "min_trading_days")
    private Integer minTradingDays;

    @Column(name = "min_profit_per_trading_day_percent")
    private BigDecimal minProfitPerTradingDayPercent;

    @Column(name = "news_trading_allowed")
    private Boolean newsTradingAllowed;

    @Column(name = "news_trading_window_minutes")
    private Integer newsTradingWindowMinutes;

    @Column(name = "weekend_holding_allowed")
    private Boolean weekendHoldingAllowed;

    @Column(name = "overnight_holding_allowed")
    private Boolean overnightHoldingAllowed;

    @Column(name = "hedging_allowed")
    private Boolean hedgingAllowed;

    @Column(name = "ea_allowed")
    private Boolean eaAllowed;

    @Column(name = "copy_trading_allowed")
    private Boolean copyTradingAllowed;

    @Column(name = "consistency_rule_percent")
    private BigDecimal consistencyRulePercent;

    @Column(name = "best_day_rule_percent")
    private BigDecimal bestDayRulePercent;

    @Column(name = "max_risk_per_trade_percent")
    private BigDecimal maxRiskPerTradePercent;

    @Column(name = "floating_loss_limit_percent")
    private BigDecimal floatingLossLimitPercent;

    @Column(name = "leverage_forex")
    private Integer leverageForex;

    @Column(name = "leverage_metals")
    private Integer leverageMetals;

    @Column(name = "leverage_indices")
    private Integer leverageIndices;

    @Column(name = "leverage_crypto")
    private Integer leverageCrypto;

    @Column(name = "leverage_energy")
    private Integer leverageEnergy;

    @Column(name = "inactivity_days")
    private Integer inactivityDays;

    @Column(name = "min_trade_duration_seconds")
    private Integer minTradeDurationSeconds;

    @Column(name = "stop_loss_required")
    private Boolean stopLossRequired;

    @Column(name = "max_stacked_trades")
    private Integer maxStackedTrades;

    @Column(name = "daily_profit_cap_usd")
    private BigDecimal dailyProfitCapUsd;

    @Column(name = "profit_split_percent")
    private BigDecimal profitSplitPercent;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis() / 1000;
    }
}