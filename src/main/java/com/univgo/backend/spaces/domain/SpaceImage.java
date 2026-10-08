package com.univgo.backend.spaces.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One photograph of a space. The row exists so that order is a decision rather than an accident:
 * the bucket could say which files a space has, but order came from S3's {@code lastModified}, so
 * the cover was whichever file happened to be uploaded first.
 *
 * <p>{@code position} is 0-based and {@code 0} is the cover. There is no separate "is cover" flag:
 * two sources for one fact drift, and reordering already says everything.
 *
 * <p>{@code originalKey} is the prefix the derivatives live under, {@code spaces/{spaceId}/{id}},
 * not a file: one upload produces one derivative per configured width. {@code width} and
 * {@code height} are the source's, which is what a layout needs to reserve the right box before
 * the bytes arrive.
 */
public record SpaceImage(
        UUID id,
        UUID spaceId,
        int position,
        String originalKey,
        String contentType,
        int width,
        int height,
        int byteSize,
        LocalDateTime createdAt,
        UUID createdBy) {

    public SpaceImage {
        if (position < 0) {
            throw new IllegalArgumentException("position must not be negative");
        }
    }

    /**
     * The prefix every derivative of one photograph lives under. The shape {@code
     * spaces/{spaceId}/{imageId}} is what {@code V19} documents, and deriving it from the ids is
     * what lets a new width be a backfill rather than a schema change.
     */
    public static String keyFor(UUID spaceId, UUID imageId) {
        return "spaces/" + spaceId + "/" + imageId;
    }

    public SpaceImage atPosition(int newPosition) {
        return new SpaceImage(
                id, spaceId, newPosition, originalKey, contentType, width, height, byteSize, createdAt, createdBy);
    }
}
