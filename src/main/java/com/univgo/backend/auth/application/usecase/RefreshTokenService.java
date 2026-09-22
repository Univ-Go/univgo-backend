package com.univgo.backend.auth.application.usecase;

import com.univgo.backend.auth.application.port.in.RefreshTokenUseCase;
import com.univgo.backend.auth.application.port.out.RefreshTokenRepositoryPort;
import com.univgo.backend.auth.application.port.out.TokenProviderPort;
import com.univgo.backend.auth.application.util.TokenHasher;
import com.univgo.backend.auth.domain.InvalidRefreshTokenException;
import com.univgo.backend.auth.domain.RefreshToken;
import com.univgo.backend.users.application.port.out.UserRepositoryPort;
import com.univgo.backend.users.domain.User;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService implements RefreshTokenUseCase {

    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final TokenProviderPort tokenProviderPort;

    public RefreshTokenService(
            RefreshTokenRepositoryPort refreshTokenRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            TokenProviderPort tokenProviderPort) {
        this.refreshTokenRepositoryPort = refreshTokenRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.tokenProviderPort = tokenProviderPort;
    }

    @Override
    public RefreshResult execute(RefreshCommand command) {
        UUID userId = tokenProviderPort.verifyRefreshToken(command.refreshToken());

        RefreshToken stored = refreshTokenRepositoryPort.findByTokenHash(TokenHasher.sha256(command.refreshToken()))
                .orElseThrow(InvalidRefreshTokenException::new);

        if (stored.isRevoked()) {
            // A rotated-out (or logged-out) token came back: someone else may hold it.
            // Kill every active token for this user, not just this one.
            refreshTokenRepositoryPort.revokeAllActiveForUser(stored.getUserId(), RefreshToken.REASON_REUSE_DETECTED);
            throw new InvalidRefreshTokenException();
        }

        if (stored.isExpired()) {
            throw new InvalidRefreshTokenException();
        }

        User user = userRepositoryPort.findById(userId).orElseThrow(InvalidRefreshTokenException::new);

        String accessToken = tokenProviderPort.generateAccessToken(user);
        TokenProviderPort.IssuedToken issuedRefreshToken = tokenProviderPort.generateRefreshToken(user);
        RefreshToken next = RefreshToken.issue(
                user.getId(), TokenHasher.sha256(issuedRefreshToken.token()), issuedRefreshToken.expiresAt());

        stored.revoke(RefreshToken.REASON_ROTATED);
        stored.markReplacedBy(next.getId());
        refreshTokenRepositoryPort.save(next);
        refreshTokenRepositoryPort.save(stored);

        return new RefreshResult(
                accessToken, issuedRefreshToken.token(), tokenProviderPort.getAccessExpirationSeconds(), user);
    }
}
