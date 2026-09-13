package com.univgo.backend.auth.infrastructure.adapter.out.persistence;

import com.univgo.backend.auth.domain.RefreshToken;

final class RefreshTokenPersistenceMapper {

    private RefreshTokenPersistenceMapper() {
    }

    static RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return new RefreshToken(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getRevokedAt(),
                entity.getRevokedReason(),
                entity.getReplacedById());
    }

    static RefreshTokenJpaEntity toEntity(RefreshToken domain) {
        return new RefreshTokenJpaEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getTokenHash(),
                domain.getExpiresAt(),
                domain.getCreatedAt(),
                domain.getRevokedAt(),
                domain.getRevokedReason(),
                domain.getReplacedById());
    }
}
