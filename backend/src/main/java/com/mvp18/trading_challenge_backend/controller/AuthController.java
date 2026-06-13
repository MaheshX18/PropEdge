package com.mvp18.trading_challenge_backend.controller;

import com.mvp18.trading_challenge_backend.dto.ApiResponse;
import com.mvp18.trading_challenge_backend.dto.AuthResponse;
import com.mvp18.trading_challenge_backend.dto.LoginRequest;
import com.mvp18.trading_challenge_backend.dto.SignupRequest;
import com.mvp18.trading_challenge_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(
            @Valid @RequestBody SignupRequest request) {
        AuthResponse response = userService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Signup successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", response));
    }
}