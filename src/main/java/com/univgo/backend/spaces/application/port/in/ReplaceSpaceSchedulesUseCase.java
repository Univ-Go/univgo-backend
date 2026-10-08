package com.univgo.backend.spaces.application.port.in;

import com.univgo.backend.spaces.application.port.in.CreateSpaceUseCase.ScheduleWindow;
import com.univgo.backend.spaces.domain.SpaceSchedule;
import java.util.List;
import java.util.UUID;

public interface ReplaceSpaceSchedulesUseCase {

    List<SpaceSchedule> execute(UUID spaceId, List<ScheduleWindow> windows);
}
