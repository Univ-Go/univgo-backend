package com.univgo.backend.auth.infrastructure.adapter.out.persistence;

import com.univgo.backend.auth.application.port.out.RefreshTokenRepositoryPort;
import com.univgo.backend.auth.domain.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    public RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository refreshTokenJpaRepository) {
        this.refreshTokenJpaRepository = refreshTokenJpaRepository;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity saved =
                refreshTokenJpaRepository.save(RefreshTokenPersistenceMapper.toEntity(refreshToken));
        return RefreshTokenPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return refreshTokenJpaRepository.findByTokenHash(tokenHash).map(RefreshTokenPersistenceMapper::toDomain);
    }

    @Override
    @Transactional
    public void revokeAllActiveForUser(UUID userId, String reason) {
        refreshTokenJpaRepository.revokeAllActiveForUser(userId, Instant.now(), reason);
    }

    @Override
    @Transactional
    public int deleteExpiredAndStaleRevoked(Instant expiredBefore, Instant revokedBefore) {
        return refreshTokenJpaRepository.deleteExpiredAndStaleRevoked(expiredBefore, revokedBefore);
    }
}
