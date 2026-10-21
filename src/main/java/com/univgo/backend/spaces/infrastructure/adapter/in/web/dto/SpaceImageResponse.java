package com.univgo.backend.spaces.infrastructure.adapter.in.web.dto;

import com.univgo.backend.spaces.domain.SpaceImageView;
import java.util.Map;
import java.util.UUID;

/**
 * @param position 0-based; position 0 is the cover. There is no separate cover flag, so reordering
 *     is the only way the cover changes.
 * @param urls one entry per derivative width, so the surface that renders it picks its own.
 */
public record SpaceImageResponse(
        UUID id, int position, Map<Integer, String> urls, int width, int height) {

    public static SpaceImageResponse from(SpaceImageView image) {
        return new SpaceImageResponse(
                image.id(), image.position(), image.urls(), image.width(), image.height());
    }
}
