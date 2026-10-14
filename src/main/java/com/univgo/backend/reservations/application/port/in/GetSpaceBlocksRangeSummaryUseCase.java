package com.univgo.backend.reservations.application.port.in;

import com.univgo.backend.reservations.domain.SpaceDaySummary;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GetSpaceBlocksRangeSummaryUseCase {

    List<SpaceDaySummary> execute(UUID spaceId, LocalDate from, LocalDate to);
}
