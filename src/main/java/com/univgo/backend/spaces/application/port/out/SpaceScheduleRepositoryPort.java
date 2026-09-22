package com.univgo.backend.spaces.application.port.out;

import com.univgo.backend.spaces.domain.SpaceSchedule;
import java.util.List;
import java.util.UUID;

public interface SpaceScheduleRepositoryPort {

    List<SpaceSchedule> findBySpaceIdAndDayOfWeek(UUID spaceId, int dayOfWeek);

    /** Every space's windows for one weekday, so listing the catalog does not ask space by space. */
    List<SpaceSchedule> findByDayOfWeek(int dayOfWeek);
}
