package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import com.univgo.backend.spaces.domain.ClosureReason;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    /** Same arrangement the reservation's own enums use: a converter writes the lower-case label,
     *  and {@code stringtype=unspecified} in the JDBC URL lets Postgres cast it to its enum type. */
    @Convert(converter = ClosureReasonConverter.class)
    @Column(nullable = false)
    private ClosureReason reason;

    @Column
    private String details;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reverted_at")
    private LocalDateTime revertedAt;

    @Column(name = "reverted_by")
    private UUID revertedBy;

    protected SpaceClosureJpaEntity() {
    }

    private SpaceClosureJpaEntity(Builder builder) {
        this.id = builder.id;
        this.spaceId = builder.spaceId;
        this.startsAt = builder.startsAt;
        this.endsAt = builder.endsAt;
        this.reason = builder.reason;
        this.details = builder.details;
        this.createdBy = builder.createdBy;
        this.createdAt = builder.createdAt;
        this.revertedAt = builder.revertedAt;
        this.revertedBy = builder.revertedBy;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public UUID getSpaceId() {
        return spaceId;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public ClosureReason getReason() {
        return reason;
    }

    public String getDetails() {
        return details;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getRevertedAt() {
        return revertedAt;
    }

    public UUID getRevertedBy() {
        return revertedBy;
    }

    public static final class Builder {

        private UUID id;
        private UUID spaceId;
        private LocalDateTime startsAt;
        private LocalDateTime endsAt;
        private ClosureReason reason;
        private String details;
        private UUID createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime revertedAt;
        private UUID revertedBy;

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder spaceId(UUID spaceId) {
            this.spaceId = spaceId;
            return this;
        }

        public Builder startsAt(LocalDateTime startsAt) {
            this.startsAt = startsAt;
            return this;
        }

        public Builder endsAt(LocalDateTime endsAt) {
            this.endsAt = endsAt;
            return this;
        }

        public Builder reason(ClosureReason reason) {
            this.reason = reason;
            return this;
        }

        public Builder details(String details) {
            this.details = details;
            return this;
        }

        public Builder createdBy(UUID createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder revertedAt(LocalDateTime revertedAt) {
            this.revertedAt = revertedAt;
            return this;
        }

        public Builder revertedBy(UUID revertedBy) {
            this.revertedBy = revertedBy;
            return this;
        }

        public SpaceClosureJpaEntity build() {
            return new SpaceClosureJpaEntity(this);
        }
    }
}
