package com.univgo.backend.reservations.domain;

import java.time.LocalDateTime;

public record OccupantView(String studentName, ReservationState state, LocalDateTime checkedInAt) {
}
