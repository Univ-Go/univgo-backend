package com.univgo.backend.auth.application.usecase;

import com.univgo.backend.auth.application.port.in.PurgeStaleRefreshTokensUseCase;
import com.univgo.backend.auth.application.port.out.RefreshTokenRepositoryPort;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class PurgeStaleRefreshTokensService implements PurgeStaleRefreshTokensUseCase {

    /** Grace period past natural expiry, in case a client is mid-refresh right at the boundary. */
    private static final Duration EXPIRED_GRACE = Duration.ofDays(1);

    /** How long a revoked row is kept around for reuse-detection forensics before it's dropped. */
    private static final Duration REVOKED_RETENTION = Duration.ofDays(7);

    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    public PurgeStaleRefreshTokensService(RefreshTokenRepositoryPort refreshTokenRepositoryPort) {
        this.refreshTokenRepositoryPort = refreshTokenRepositoryPort;
    }

    @Override
    public int execute() {
        Instant now = Instant.now();
        return refreshTokenRepositoryPort.deleteExpiredAndStaleRevoked(
                now.minus(EXPIRED_GRACE), now.minus(REVOKED_RETENTION));
    }
}
