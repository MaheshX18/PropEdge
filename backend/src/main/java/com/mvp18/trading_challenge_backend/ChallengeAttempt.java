package com.mvp18.trading_challenge_backend;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "challenge_attempts")
public class ChallengeAttempt {

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
    @JoinColumn(name = "challenge_rules_id", nullable = false)
    private ChallengeRules challengeRules;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    @Column(name = "started_at", nullable = false)
    private Long startedAt;

    @Column(name = "ended_at")
    private Long endedAt;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis() / 1000;
        startedAt = createdAt;
    }
}