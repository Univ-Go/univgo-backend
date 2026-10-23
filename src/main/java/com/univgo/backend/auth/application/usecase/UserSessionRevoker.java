package com.univgo.backend.auth.application.usecase;

import com.univgo.backend.auth.application.port.out.RefreshTokenRepositoryPort;
import com.univgo.backend.auth.domain.RefreshToken;
import com.univgo.backend.users.application.port.out.UserSessionRevokerPort;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Fulfils the {@code users} module's "log this user out everywhere". It lives here because sessions
 * are refresh tokens and those are here; the port lives there because {@code users} is the side
 * that asks.
 */
@Service
public class UserSessionRevoker implements UserSessionRevokerPort {

    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    public UserSessionRevoker(RefreshTokenRepositoryPort refreshTokenRepositoryPort) {
        this.refreshTokenRepositoryPort = refreshTokenRepositoryPort;
    }

    @Override
    public void revokeAllSessions(UUID userId) {
        refreshTokenRepositoryPort.revokeAllActiveForUser(userId, RefreshToken.REASON_PASSWORD_CHANGED);
    }
}
