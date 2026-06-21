package com.mvp18.trading_challenge_backend.controller;

import com.mvp18.trading_challenge_backend.ChallengeAttempt;
import com.mvp18.trading_challenge_backend.ChallengeRules;
import com.mvp18.trading_challenge_backend.security.SecurityUtils;
import com.mvp18.trading_challenge_backend.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/challenge")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    // GET /api/challenge/rules - public
    @GetMapping("/rules")
    public ResponseEntity<List<ChallengeRules>> getAllRules() {
        return ResponseEntity.ok(challengeService.getAllRules());
    }

    // POST /api/challenge/start - requires JWT
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startChallenge(
            @RequestBody Map<String, Object> request) {
        // Email from JWT token automatically!
        String email = SecurityUtils.getCurrentUserEmail();
        BigDecimal accountSize = new BigDecimal(
                request.get("accountSize").toString());
        return ResponseEntity.ok(
                challengeService.startChallenge(email, accountSize));
    }

    // GET /api/challenge/my-challenges - requires JWT
    @GetMapping("/my-challenges")
    public ResponseEntity<List<ChallengeAttempt>> getMyChallenges() {
        String email = SecurityUtils.getCurrentUserEmail();
        return ResponseEntity.ok(
                challengeService.getUserChallenges(email));
    }
}