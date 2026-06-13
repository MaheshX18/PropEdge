package com.mvp18.trading_challenge_backend.service.interfaces;

import com.mvp18.trading_challenge_backend.dto.AuthResponse;
import com.mvp18.trading_challenge_backend.dto.LoginRequest;
import com.mvp18.trading_challenge_backend.dto.SignupRequest;

public interface IUserService {
    AuthResponse signup(SignupRequest request);
    AuthResponse login(LoginRequest request);
}