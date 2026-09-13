package com.univgo.backend.auth.application.port.in;

public interface RefreshTokenUseCase {

    RefreshResult execute(RefreshCommand command);

    record RefreshCommand(String refreshToken) {
    }

    record RefreshResult(String accessToken, String refreshToken, long expiresIn) {
    }
}
