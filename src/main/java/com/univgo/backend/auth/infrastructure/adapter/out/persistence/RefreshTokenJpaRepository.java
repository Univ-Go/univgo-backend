package com.univgo.backend.auth.infrastructure.adapter.out.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {

    Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE RefreshTokenJpaEntity r SET r.revokedAt = :revokedAt, r.revokedReason = :reason "
            + "WHERE r.userId = :userId AND r.revokedAt IS NULL")
    void revokeAllActiveForUser(
            @Param("userId") UUID userId, @Param("revokedAt") Instant revokedAt, @Param("reason") String reason);

    @Modifying
    @Query("DELETE FROM RefreshTokenJpaEntity r WHERE r.expiresAt < :expiredBefore "
            + "OR (r.revokedAt IS NOT NULL AND r.revokedAt < :revokedBefore)")
    int deleteExpiredAndStaleRevoked(
            @Param("expiredBefore") Instant expiredBefore, @Param("revokedBefore") Instant revokedBefore);
}
