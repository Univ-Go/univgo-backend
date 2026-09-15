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

    Optional<Reservation> findByQrCodeData(String qrCodeData);

    Reservation save(Reservation reservation);

    boolean existsOverlappingForUser(UUID userId, LocalDate date, LocalTime startTime, LocalTime endTime);

    long countActiveByUserSpaceAndDate(UUID userId, UUID spaceId, LocalDate date);

    List<Reservation> findActiveByBlock(UUID spaceId, LocalDate date, LocalTime startTime, LocalTime endTime);

    List<Reservation> findActiveBySpaceId(UUID spaceId);
}
