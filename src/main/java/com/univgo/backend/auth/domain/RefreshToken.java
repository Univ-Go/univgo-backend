package com.univgo.backend.auth.domain;

import java.time.Instant;
import java.util.UUID;

public class RefreshToken {

    public static final String REASON_ROTATED = "ROTATED";
    public static final String REASON_LOGOUT = "LOGOUT";
    public static final String REASON_REUSE_DETECTED = "REUSE_DETECTED";
    public static final String REASON_PASSWORD_CHANGED = "PASSWORD_CHANGED";

    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final Instant expiresAt;
    private final Instant createdAt;
    private Instant revokedAt;
    private String revokedReason;
    private UUID replacedById;

    private RefreshToken(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.tokenHash = builder.tokenHash;
        this.expiresAt = builder.expiresAt;
        this.createdAt = builder.createdAt;
        this.revokedAt = builder.revokedAt;
        this.revokedReason = builder.revokedReason;
        this.replacedById = builder.replacedById;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static RefreshToken issue(UUID userId, String tokenHash, Instant expiresAt) {
        return builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .createdAt(Instant.now())
                .build();
    }

    public static final class Builder {
        private UUID id;
        private UUID userId;
        private String tokenHash;
        private Instant expiresAt;
        private Instant createdAt;
        private Instant revokedAt;
        private String revokedReason;
        private UUID replacedById;

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder tokenHash(String tokenHash) {
            this.tokenHash = tokenHash;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder revokedAt(Instant revokedAt) {
            this.revokedAt = revokedAt;
            return this;
        }

        public Builder revokedReason(String revokedReason) {
            this.revokedReason = revokedReason;
            return this;
        }

        public Builder replacedById(UUID replacedById) {
            this.replacedById = replacedById;
            return this;
        }

        public RefreshToken build() {
            return new RefreshToken(this);
        }
    }

    public void revoke(String reason) {
        this.revokedAt = Instant.now();
        this.revokedReason = reason;
    }

    public void markReplacedBy(UUID newTokenId) {
        this.replacedById = newTokenId;
    }

    public boolean isRevoked() {
        return revokedAt != null;
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
        return revokedAt;
    }

    public String getRevokedReason() {
        return revokedReason;
    }

    public UUID getReplacedById() {
        return replacedById;
    }
}
