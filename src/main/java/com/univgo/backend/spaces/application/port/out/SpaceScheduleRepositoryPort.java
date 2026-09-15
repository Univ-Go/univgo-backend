package com.univgo.backend.spaces.application.port.out;

import com.univgo.backend.spaces.domain.SpaceSchedule;
import java.util.List;
import java.util.UUID;

public interface SpaceScheduleRepositoryPort {

    List<SpaceSchedule> findBySpaceIdAndDayOfWeek(UUID spaceId, int dayOfWeek);
}
