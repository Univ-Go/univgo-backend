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

    /**
     * Every active reservation of a day, across spaces. Reading a whole day at once is what keeps the
     * catalog to a handful of round trips: asking block by block cost one query per block per space.
     */
    List<Reservation> findActiveByDate(LocalDate date);

    List<Reservation> findActiveBySpaceAndDate(UUID spaceId, LocalDate date);

    List<Reservation> findActiveByUserAndDate(UUID userId, LocalDate date);

    List<Reservation> findActiveBySpaceId(UUID spaceId);
}
