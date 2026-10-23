package com.univgo.backend.spaces.domain;

import java.util.List;

/** Everything the edit screen loads: the space, its week of windows and its photographs in order. */
public record AdminSpaceDetail(
        AdminSpaceView space, List<SpaceSchedule> schedules, List<SpaceImageView> images) {

    public AdminSpaceDetail {
        schedules = List.copyOf(schedules);
        images = List.copyOf(images);
    }
}
