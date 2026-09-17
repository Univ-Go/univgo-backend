package com.univgo.backend.auth.application.port.in;

import com.univgo.backend.users.domain.User;

public interface RefreshTokenUseCase {

    RefreshResult execute(RefreshCommand command);

    record RefreshCommand(String refreshToken) {
    }

    /** Mirrors {@link LoginUseCase.LoginResult}: a refresh re-describes the session it renews. */
    record RefreshResult(String accessToken, String refreshToken, long expiresIn, User user) {
    }
}
