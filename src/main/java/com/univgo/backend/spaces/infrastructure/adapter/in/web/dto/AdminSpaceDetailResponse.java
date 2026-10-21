package com.univgo.backend.spaces.infrastructure.adapter.in.web.dto;

import com.univgo.backend.spaces.domain.AdminSpaceDetail;
import com.univgo.backend.spaces.domain.SpaceCategory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AdminSpaceDetailResponse(
        UUID spaceId,
        String name,
        String location,
        UUID spaceTypeId,
        String spaceTypeName,
        SpaceCategory category,
        int capacity,
        String description,
        List<String> rules,
        boolean archived,
        LocalDateTime archivedAt,
        List<ScheduleWindowResponse> schedules,
        List<SpaceImageResponse> images) {

    public static AdminSpaceDetailResponse from(AdminSpaceDetail detail) {
        return new AdminSpaceDetailResponse(
                detail.space().spaceId(),
                detail.space().name(),
                detail.space().location(),
                detail.space().spaceTypeId(),
                detail.space().spaceTypeName(),
                detail.space().category(),
                detail.space().capacity(),
                detail.space().description(),
                detail.space().rules(),
                detail.space().archived(),
                detail.space().archivedAt(),
                detail.schedules().stream().map(ScheduleWindowResponse::from).toList(),
                detail.images().stream().map(SpaceImageResponse::from).toList());
    }
}
