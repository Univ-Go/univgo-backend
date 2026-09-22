package com.univgo.backend.reservations.domain;

import java.time.LocalDateTime;

public record OccupantView(
        String studentName, String document, String school, ReservationState state, LocalDateTime checkedInAt) {
}
