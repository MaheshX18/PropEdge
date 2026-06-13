package com.mvp18.trading_challenge_backend.service;

import com.mvp18.trading_challenge_backend.service.interfaces.IUserService;
import com.mvp18.trading_challenge_backend.dto.AuthResponse;
import com.mvp18.trading_challenge_backend.User;
import com.mvp18.trading_challenge_backend.dto.LoginRequest;
import com.mvp18.trading_challenge_backend.dto.SignupRequest;
import com.mvp18.trading_challenge_backend.repository.UserRepository;
import com.mvp18.trading_challenge_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse signup(SignupRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User already exists with this email");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setCountry(request.getCountry());
        user.setBalance(BigDecimal.ZERO);
        user.setKycVerified(false);

        // Save to database
        User savedUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getEmail());

        // Return response
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                token,
                "Bearer"
        );
    }

    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Generate token
        String token = jwtUtil.generateToken(user.getEmail());

        // Return response
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                token,
                "Bearer"
        );
    }
}