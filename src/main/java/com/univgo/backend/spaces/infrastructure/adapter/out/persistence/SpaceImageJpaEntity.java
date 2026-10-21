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
    public SpaceImageJpaEntity(
            UUID id,
            UUID spaceId,
            short position,
            String originalKey,
            String contentType,
            Integer width,
            Integer height,
            Integer byteSize,
            LocalDateTime createdAt,
            UUID createdBy) {
        this.id = id;
        this.spaceId = spaceId;
        this.position = position;
        this.originalKey = originalKey;
        this.contentType = contentType;
        this.width = width;
        this.height = height;
        this.byteSize = byteSize;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
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
}
