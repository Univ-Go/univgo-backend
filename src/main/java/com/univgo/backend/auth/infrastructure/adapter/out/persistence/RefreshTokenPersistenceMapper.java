package com.univgo.backend.auth.infrastructure.adapter.out.persistence;

import com.univgo.backend.auth.domain.RefreshToken;

final class RefreshTokenPersistenceMapper {

    private RefreshTokenPersistenceMapper() {
    }

    static RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return RefreshToken.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .tokenHash(entity.getTokenHash())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .revokedAt(entity.getRevokedAt())
                .revokedReason(entity.getRevokedReason())
                .replacedById(entity.getReplacedById())
                .build();
    }

    static RefreshTokenJpaEntity toEntity(RefreshToken domain) {
        return RefreshTokenJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .tokenHash(domain.getTokenHash())
                .expiresAt(domain.getExpiresAt())
                .createdAt(domain.getCreatedAt())
                .revokedAt(domain.getRevokedAt())
                .revokedReason(domain.getRevokedReason())
                .replacedById(domain.getReplacedById())
                .build();
    }
}
