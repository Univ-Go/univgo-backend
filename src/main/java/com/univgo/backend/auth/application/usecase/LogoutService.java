package com.univgo.backend.auth.application.usecase;

import com.univgo.backend.auth.application.port.in.LogoutUseCase;
import com.univgo.backend.auth.application.port.out.RefreshTokenRepositoryPort;
import com.univgo.backend.auth.application.util.TokenHasher;
import com.univgo.backend.auth.domain.RefreshToken;
import org.springframework.stereotype.Service;

@Service
public class LogoutService implements LogoutUseCase {

    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    public LogoutService(RefreshTokenRepositoryPort refreshTokenRepositoryPort) {
        this.refreshTokenRepositoryPort = refreshTokenRepositoryPort;
    }

    @Override
    public void execute(LogoutCommand command) {
        // Idempotent and silent either way: an unknown or already-revoked token
        // shouldn't tell the caller anything about its validity.
        refreshTokenRepositoryPort.findByTokenHash(TokenHasher.sha256(command.refreshToken()))
                .filter(token -> !token.isRevoked())
                .ifPresent(token -> {
                    token.revoke(RefreshToken.REASON_LOGOUT);
                    refreshTokenRepositoryPort.save(token);
                });
    }
}
