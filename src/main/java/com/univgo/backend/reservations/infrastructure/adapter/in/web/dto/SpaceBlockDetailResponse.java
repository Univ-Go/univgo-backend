package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import com.univgo.backend.reservations.domain.OccupantView;
import com.univgo.backend.reservations.domain.ReservationState;
import com.univgo.backend.reservations.domain.SpaceBlockDetail;
import com.univgo.backend.spaces.domain.ClosureReason;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record SpaceBlockDetailResponse(
        LocalTime start,
        LocalTime end,
        int capacity,
        int occupied,
        int free,
        boolean closed,
        ClosureReason closureReason,
        List<OccupantResponse> roster) {

    public static SpaceBlockDetailResponse from(SpaceBlockDetail detail) {
        return new SpaceBlockDetailResponse(
                detail.block().start(),
                detail.block().end(),
                detail.capacity(),
                detail.occupied(),
                detail.free(),
                detail.closed(),
                detail.closureReason(),
                detail.roster().stream().map(OccupantResponse::from).toList());
    }

    public record OccupantResponse(
            String studentName,
            String document,
            String school,
            ReservationState state,
            LocalDateTime checkedInAt) {
        public static OccupantResponse from(OccupantView occupant) {
            return new OccupantResponse(
                    occupant.studentName(),
                    occupant.document(),
                    occupant.school(),
                    occupant.state(),
                    occupant.checkedInAt());
        }
    }
}
