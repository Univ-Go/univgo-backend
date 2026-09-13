package com.univgo.backend.auth.application.port.out;

import com.univgo.backend.users.domain.User;
import java.time.Instant;
import java.util.UUID;

public interface TokenProviderPort {

    String generateAccessToken(User user);

    IssuedToken generateRefreshToken(User user);

    long getAccessExpirationSeconds();

    long getRefreshExpirationSeconds();

    /**
     * Verifies signature, expiry and token type ("refresh"), rejecting an
     * access token replayed here. Returns the subject as the user id.
     * Throws {@code InvalidRefreshTokenException} on any failure.
     */
    UUID verifyRefreshToken(String token);

    record IssuedToken(String token, Instant expiresAt) {
    }
}
