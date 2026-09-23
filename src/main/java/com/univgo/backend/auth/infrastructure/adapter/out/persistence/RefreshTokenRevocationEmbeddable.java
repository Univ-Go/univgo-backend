package com.univgo.backend.auth.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;
import java.util.UUID;

@Embeddable
class RefreshTokenRevocationEmbeddable {

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoked_reason", length = 20)
    private String revokedReason;

    @Column(name = "replaced_by_id")
    private UUID replacedById;

    protected RefreshTokenRevocationEmbeddable() {
    }

    RefreshTokenRevocationEmbeddable(Instant revokedAt, String revokedReason, UUID replacedById) {
        this.revokedAt = revokedAt;
        this.revokedReason = revokedReason;
        this.replacedById = replacedById;
    }

    Instant getRevokedAt() {
        return revokedAt;
    }

    String getRevokedReason() {
        return revokedReason;
    }

    UUID getReplacedById() {
        return replacedById;
    }
}
