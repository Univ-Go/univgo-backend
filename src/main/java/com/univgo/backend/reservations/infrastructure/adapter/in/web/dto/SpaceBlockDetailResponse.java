package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import com.univgo.backend.reservations.domain.OccupantView;
import com.univgo.backend.reservations.domain.ReservationState;
import com.univgo.backend.reservations.domain.SpaceBlockDetail;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record SpaceBlockDetailResponse(
        LocalTime start, LocalTime end, int capacity, int occupied, int free, List<OccupantResponse> roster) {

    public static SpaceBlockDetailResponse from(SpaceBlockDetail detail) {
        return new SpaceBlockDetailResponse(
                detail.block().start(),
                detail.block().end(),
                detail.capacity(),
                detail.occupied(),
                detail.free(),
                detail.roster().stream().map(OccupantResponse::from).toList());
    }

    public record OccupantResponse(String studentName, ReservationState state, LocalDateTime checkedInAt) {
        public static OccupantResponse from(OccupantView occupant) {
            return new OccupantResponse(occupant.studentName(), occupant.state(), occupant.checkedInAt());
        }
    }
}
