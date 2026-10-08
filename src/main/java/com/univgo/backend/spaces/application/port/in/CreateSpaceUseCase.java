package com.univgo.backend.spaces.application.port.in;

import com.univgo.backend.spaces.application.port.out.ImageUpload;
import com.univgo.backend.spaces.domain.AdminSpaceDetail;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface CreateSpaceUseCase {

    AdminSpaceDetail execute(CreateSpaceCommand command);

    /**
     * Everything the creation wizard collected, in one command. The wizard holds its answers in the
     * browser and sends them once, so a space is never visible half-configured: there is no draft
     * state to leave behind.
     *
     * @param photos in the order the administrator arranged them; the first becomes the cover.
     */
    record CreateSpaceCommand(
            String name,
            String location,
            UUID spaceTypeId,
            int capacity,
            String description,
            List<String> rules,
            List<ScheduleWindow> schedules,
            List<ImageUpload> photos,
            UUID actor) {
    }

    record ScheduleWindow(int dayOfWeek, LocalTime startTime, LocalTime endTime) {
    }
}
