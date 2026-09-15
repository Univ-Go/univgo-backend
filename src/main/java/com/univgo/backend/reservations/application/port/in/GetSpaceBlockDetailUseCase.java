package com.univgo.backend.reservations.application.port.in;

import com.univgo.backend.reservations.domain.SpaceBlockDetail;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public interface GetSpaceBlockDetailUseCase {

    SpaceBlockDetail execute(UUID spaceId, LocalDate date, LocalTime blockStart);
}
