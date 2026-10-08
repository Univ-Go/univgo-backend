package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import com.univgo.backend.reservations.domain.SpaceCatalogItem;
import com.univgo.backend.spaces.domain.SpaceCategory;
import com.univgo.backend.spaces.domain.SpaceImageView;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record SpaceCatalogResponse(
        UUID spaceId,
        String name,
        String location,
        SpaceCategory category,
        int capacity,
        boolean underMaintenance,
        boolean opensOnDate,
        boolean closedOnDate,
        List<LocalTime> freeBlockStarts,
        List<SpaceImageResponse> images,
        String description,
        List<String> rules,
        boolean archived) {

    /**
     * The catalogue's own photograph shape. Flat on purpose: a student view needs the URLs and the
     * source dimensions, not the row's id or its position — the order of the list already says
     * which one is the cover.
     */
    public record SpaceImageResponse(java.util.Map<Integer, String> urls, int width, int height) {
    }

    private static SpaceImageResponse toImage(SpaceImageView image) {
        return new SpaceImageResponse(image.urls(), image.width(), image.height());
    }

    public static SpaceCatalogResponse from(SpaceCatalogItem item) {
        return new SpaceCatalogResponse(
                item.spaceId(),
                item.name(),
                item.location(),
                item.category(),
                item.capacity(),
                item.underMaintenance(),
                item.opensOnDate(),
                item.closedOnDate(),
                item.freeBlockStarts(),
                item.images().stream().map(SpaceCatalogResponse::toImage).toList(),
                item.description(),
                item.rules(),
                item.archived());
    }
}
