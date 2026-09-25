package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable
class SpaceClosurePeriodEmbeddable {

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    protected SpaceClosurePeriodEmbeddable() {
    }

    SpaceClosurePeriodEmbeddable(LocalDateTime startsAt, LocalDateTime endsAt) {
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    LocalDateTime getStartsAt() {
        return startsAt;
    }

    LocalDateTime getEndsAt() {
        return endsAt;
    }
}
