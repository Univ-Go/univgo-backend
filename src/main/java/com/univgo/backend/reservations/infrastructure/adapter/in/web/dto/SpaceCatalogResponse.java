package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import com.univgo.backend.reservations.domain.SpaceCatalogItem;
import com.univgo.backend.spaces.domain.SpaceCategory;
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
        List<String> images,
        String description,
        List<String> rules) {

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
                item.images(),
                item.description(),
                item.rules());
    }
}
