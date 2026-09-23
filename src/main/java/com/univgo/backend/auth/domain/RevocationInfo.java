package com.univgo.backend.auth.domain;

import java.time.Instant;
import java.util.UUID;

public record RevocationInfo(Instant revokedAt, String revokedReason, UUID replacedById) {

    public static RevocationInfo none() {
        return new RevocationInfo(null, null, null);
    }

    public RevocationInfo revoke(String reason) {
        return new RevocationInfo(Instant.now(), reason, this.replacedById);
    }

    public RevocationInfo withReplacedBy(UUID newTokenId) {
        return new RevocationInfo(this.revokedAt, this.revokedReason, newTokenId);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }
}
