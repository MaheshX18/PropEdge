package com.mvp18.trading_challenge_backend.repository;

import com.mvp18.trading_challenge_backend.ChallengeAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChallengeAttemptRepository extends JpaRepository<ChallengeAttempt, Long> {
    List<ChallengeAttempt> findByUserId(Long userId);
    List<ChallengeAttempt> findByUserIdAndStatus(Long userId, String status);
}