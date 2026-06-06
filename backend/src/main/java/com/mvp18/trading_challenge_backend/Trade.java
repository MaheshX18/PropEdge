package com.mvp18.trading_challenge_backend;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "trades")
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trading_account_id", nullable = false)
    private TradingAccount tradingAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_attempt_id", nullable = false)
    private ChallengeAttempt challengeAttempt;

    @Column(name = "symbol", nullable = false)
    private String symbol;

    // BUY or SELL
    @Column(name = "direction", nullable = false)
    private String direction;

    @Column(name = "lot_size", nullable = false)
    private BigDecimal lotSize;

    @Column(name = "open_price", nullable = false)
    private BigDecimal openPrice;

    @Column(name = "close_price")
    private BigDecimal closePrice;

    @Column(name = "stop_loss")
    private BigDecimal stopLoss;

    @Column(name = "take_profit")
    private BigDecimal takeProfit;

    @Column(name = "profit_loss")
    private BigDecimal profitLoss;

    // OPEN or CLOSED
    @Column(name = "status", nullable = false)
    private String status = "OPEN";

    @Column(name = "opened_at", nullable = false)
    private Long openedAt;

    @Column(name = "closed_at")
    private Long closedAt;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis() / 1000;
        openedAt = createdAt;
    }
}