package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import java.util.UUID;

@Embeddable
class SpaceClosureReversionEmbeddable {

    @Column(name = "reverted_at")
    private LocalDateTime revertedAt;

    @Column(name = "reverted_by")
    private UUID revertedBy;

    protected SpaceClosureReversionEmbeddable() {
    }

    SpaceClosureReversionEmbeddable(LocalDateTime revertedAt, UUID revertedBy) {
        this.revertedAt = revertedAt;
        this.revertedBy = revertedBy;
    }

    LocalDateTime getRevertedAt() {
        return revertedAt;
    }

    UUID getRevertedBy() {
        return revertedBy;
    }
}
