package com.univgo.backend.auth.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoked_reason", length = 20)
    private String revokedReason;

    @Column(name = "replaced_by_id")
    private UUID replacedById;

    protected RefreshTokenJpaEntity() {
    }

    private RefreshTokenJpaEntity(Builder builder) {
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

        public RefreshTokenJpaEntity build() {
            return new RefreshTokenJpaEntity(this);
        }
    }
}
