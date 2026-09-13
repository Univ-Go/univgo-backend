package com.univgo.backend.auth.infrastructure.adapter.in.web.dto;

import com.univgo.backend.auth.application.port.in.RefreshTokenUseCase.RefreshResult;

public record RefreshTokenResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {

    public static RefreshTokenResponse from(RefreshResult result) {
        return new RefreshTokenResponse(result.accessToken(), result.refreshToken(), "Bearer", result.expiresIn());
    }
}
