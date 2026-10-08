package com.univgo.backend.spaces.infrastructure.adapter.in.web.dto;

import com.univgo.backend.spaces.domain.AdminSpaceSummary;
import com.univgo.backend.spaces.domain.SpaceCategory;
import java.util.UUID;

/**
 * @param coverImageUrl null for a space with no photographs. That cannot happen through the
 *     creation wizard, which requires one, but it can for a space seeded before the CRUD existed.
 */
public record AdminSpaceSummaryResponse(
        UUID spaceId,
        String name,
        String location,
        UUID spaceTypeId,
        String spaceTypeName,
        SpaceCategory category,
        int capacity,
        int scheduleWindowCount,
        int imageCount,
        String coverImageUrl,
        boolean archived) {

    public static AdminSpaceSummaryResponse from(AdminSpaceSummary summary) {
        return new AdminSpaceSummaryResponse(
                summary.space().spaceId(),
                summary.space().name(),
                summary.space().location(),
                summary.space().spaceTypeId(),
                summary.space().spaceTypeName(),
                summary.space().category(),
                summary.space().capacity(),
                summary.scheduleWindowCount(),
                summary.imageCount(),
                summary.coverImageUrl(),
                summary.space().archived());
    }
}
