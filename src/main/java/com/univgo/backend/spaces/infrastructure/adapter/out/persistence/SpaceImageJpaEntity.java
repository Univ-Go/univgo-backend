package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "space_images")
public class SpaceImageJpaEntity {

    @Id
    private UUID id;

    @Column(name = "space_id", nullable = false)
    private UUID spaceId;

    @Column(nullable = false)
    private short position;

    @Column(name = "original_key", nullable = false, columnDefinition = "text")
    private String originalKey;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Integer width;

    @Column(nullable = false)
    private Integer height;

    @Column(name = "byte_size", nullable = false)
    private Integer byteSize;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    protected SpaceImageJpaEntity() {
    }

    /** All-args and no setters, as the other entities in this package: a row is replaced, not mutated. */
    private SpaceImageJpaEntity(Builder builder) {
        this.id = builder.id;
        this.spaceId = builder.spaceId;
        this.position = builder.position;
        this.originalKey = builder.originalKey;
        this.contentType = builder.contentType;
        this.width = builder.width;
        this.height = builder.height;
        this.byteSize = builder.byteSize;
        this.createdAt = builder.createdAt;
        this.createdBy = builder.createdBy;
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

    public short getPosition() {
        return position;
    }

    public String getOriginalKey() {
        return originalKey;
    }

    public String getContentType() {
        return contentType;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public Integer getByteSize() {
        return byteSize;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public static final class Builder {
        private UUID id;
        private UUID spaceId;
        private short position;
        private String originalKey;
        private String contentType;
        private Integer width;
        private Integer height;
        private Integer byteSize;
        private LocalDateTime createdAt;
        private UUID createdBy;

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

        public Builder position(short position) {
            this.position = position;
            return this;
        }

        public Builder originalKey(String originalKey) {
            this.originalKey = originalKey;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder width(Integer width) {
            this.width = width;
            return this;
        }

        public Builder height(Integer height) {
            this.height = height;
            return this;
        }

        public Builder byteSize(Integer byteSize) {
            this.byteSize = byteSize;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder createdBy(UUID createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public SpaceImageJpaEntity build() {
            return new SpaceImageJpaEntity(this);
        }
    }
}
