package com.univgo.backend.reservations.application.port.out;

import com.univgo.backend.reservations.domain.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepositoryPort {

    List<Reservation> findAll();

    List<Reservation> findByUserId(UUID userId);

    Optional<Reservation> findById(UUID id);

    Reservation save(Reservation reservation);

    void deleteById(UUID id);

    boolean existsOverlapping(UUID spaceId, LocalDate date, LocalTime startTime, LocalTime endTime);
}
