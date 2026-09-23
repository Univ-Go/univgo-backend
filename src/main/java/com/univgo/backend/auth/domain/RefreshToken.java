package com.univgo.backend.auth.domain;

import java.time.Instant;
import java.util.UUID;

public class RefreshToken {

    public static final String REASON_ROTATED = "ROTATED";
    public static final String REASON_LOGOUT = "LOGOUT";
    public static final String REASON_REUSE_DETECTED = "REUSE_DETECTED";

    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final Instant expiresAt;
    private final Instant createdAt;
    private RevocationInfo revocationInfo;

    public RefreshToken(
            UUID id,
            UUID userId,
            String tokenHash,
            Instant expiresAt,
            Instant createdAt,
            RevocationInfo revocationInfo) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.revocationInfo = revocationInfo;
    }

    public static RefreshToken issue(UUID userId, String tokenHash, Instant expiresAt) {
        return new RefreshToken(UUID.randomUUID(), userId, tokenHash, expiresAt, Instant.now(), RevocationInfo.none());
    }

    public void revoke(String reason) {
        this.revocationInfo = this.revocationInfo.revoke(reason);
    }

    public void markReplacedBy(UUID newTokenId) {
        this.revocationInfo = this.revocationInfo.withReplacedBy(newTokenId);
    }

    public boolean isRevoked() {
        return revocationInfo.isRevoked();
    }

    public boolean isExpired() {
        return !expiresAt.isAfter(Instant.now());
    }

    public boolean isActive() {
        return !isRevoked() && !isExpired();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getRevokedAt() {
        return revocationInfo.revokedAt();
    }

    public String getRevokedReason() {
        return revocationInfo.revokedReason();
    }

    public UUID getReplacedById() {
        return revocationInfo.replacedById();
    }
}
