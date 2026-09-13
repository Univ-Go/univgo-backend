package com.univgo.backend.auth.application.port.out;

import com.univgo.backend.auth.domain.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepositoryPort {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Revokes every still-active refresh token for a user, e.g. on reuse detection. */
    void revokeAllActiveForUser(UUID userId, String reason);

    /**
     * Deletes rows no longer needed: naturally expired ones, and revoked ones
     * past their forensic-retention window. Returns rows deleted.
     */
    int deleteExpiredAndStaleRevoked(Instant expiredBefore, Instant revokedBefore);
}
