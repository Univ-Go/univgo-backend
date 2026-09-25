package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import com.univgo.backend.spaces.domain.ClosureReason;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "space_closures")
public class SpaceClosureJpaEntity {

    @Id
    private UUID id;

    @Column(name = "space_id", nullable = false)
    private UUID spaceId;

    @Embedded
    private SpaceClosurePeriodEmbeddable period;

    @Embedded
    private SpaceClosureCauseEmbeddable cause;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Embedded
    private SpaceClosureReversionEmbeddable reversion;

    protected SpaceClosureJpaEntity() {
    }

    public SpaceClosureJpaEntity(
            UUID id,
            UUID spaceId,
            SpaceClosurePeriodEmbeddable period,
            SpaceClosureCauseEmbeddable cause,
            UUID createdBy,
            LocalDateTime createdAt,
            SpaceClosureReversionEmbeddable reversion) {
        this.id = id;
        this.spaceId = spaceId;
        this.period = period;
        this.cause = cause;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.reversion = reversion;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSpaceId() {
        return spaceId;
    }

    public LocalDateTime getStartsAt() {
        return period.getStartsAt();
    }

    public LocalDateTime getEndsAt() {
        return period.getEndsAt();
    }

    public ClosureReason getReason() {
        return cause.getReason();
    }

    public String getDetails() {
        return cause.getDetails();
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getRevertedAt() {
        return reversion.getRevertedAt();
    }

    public UUID getRevertedBy() {
        return reversion.getRevertedBy();
    }
}
