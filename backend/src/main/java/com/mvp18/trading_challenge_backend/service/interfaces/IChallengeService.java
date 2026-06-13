package com.mvp18.trading_challenge_backend.service.interfaces;

import com.mvp18.trading_challenge_backend.ChallengeRules;
import com.mvp18.trading_challenge_backend.dto.ChallengeResponse;
import java.math.BigDecimal;
import java.util.List;

public interface IChallengeService {
    List<ChallengeRules> getAllRules();
    List<ChallengeRules> getRulesByFirm(String firmName);
    ChallengeResponse startChallenge(String email,
                                     BigDecimal accountSize, String firmName,
                                     String challengeType);
    List<ChallengeResponse> getUserChallenges(String email);
}