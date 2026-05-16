package com.mvp18.trading_challenge_backend.controller;

import com.mvp18.trading_challenge_backend.ChallengeAttempt;
import com.mvp18.trading_challenge_backend.ChallengeRules;
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

    // GET /api/challenge/rules
    @GetMapping("/rules")
    public ResponseEntity<List<ChallengeRules>> getAllRules() {
        return ResponseEntity.ok(challengeService.getAllRules());
    }

    // POST /api/challenge/start
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startChallenge(@RequestBody Map<String, Object> request) {
        String email = (String) request.get("email");
        BigDecimal accountSize = new BigDecimal(request.get("accountSize").toString());
        return ResponseEntity.ok(challengeService.startChallenge(email, accountSize));
    }

    // GET /api/challenge/my-challenges?email=xxx
    @GetMapping("/my-challenges")
    public ResponseEntity<List<ChallengeAttempt>> getMyChallenges(@RequestParam String email) {
        return ResponseEntity.ok(challengeService.getUserChallenges(email));
    }
}