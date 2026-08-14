package com.univgo.backend.auth.infrastructure.adapter.in.web.dto;

import com.univgo.backend.auth.application.port.in.LoginUseCase.LoginResult;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {

    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(result.accessToken(), "Bearer", result.expiresIn());
    }
}
