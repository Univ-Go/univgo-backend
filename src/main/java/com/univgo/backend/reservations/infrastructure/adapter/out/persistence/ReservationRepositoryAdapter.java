package com.univgo.backend.reservations.infrastructure.adapter.out.persistence;

import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ReservationRepositoryAdapter implements ReservationRepositoryPort {

    private final ReservationJpaRepository reservationJpaRepository;

    public ReservationRepositoryAdapter(ReservationJpaRepository reservationJpaRepository) {
        this.reservationJpaRepository = reservationJpaRepository;
    }

    @Override
    public List<Reservation> findAll() {
        return reservationJpaRepository.findAll().stream()
                .map(ReservationPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Reservation> findByUserId(UUID userId) {
        return reservationJpaRepository.findByUserId(userId).stream()
                .map(ReservationPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Reservation> findById(UUID id) {
        return reservationJpaRepository.findById(id).map(ReservationPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Reservation> findByQrCodeData(String qrCodeData) {
        return reservationJpaRepository.findByQrCodeData(qrCodeData).map(ReservationPersistenceMapper::toDomain);
    }

    @Override
    public Reservation save(Reservation reservation) {
        var saved = reservationJpaRepository.save(ReservationPersistenceMapper.toEntity(reservation));
        return ReservationPersistenceMapper.toDomain(saved);
    }

    @Override
    public List<Reservation> findActiveByDate(LocalDate date) {
        return toDomain(reservationJpaRepository.findByReservationDateAndCancelledAtIsNull(date));
    }

    @Override
    public List<Reservation> findActiveBySpaceAndDate(UUID spaceId, LocalDate date) {
        return toDomain(reservationJpaRepository.findBySpaceIdAndReservationDateAndCancelledAtIsNull(spaceId, date));
    }

    @Override
    public List<Reservation> findActiveByUserAndDate(UUID userId, LocalDate date) {
        return toDomain(reservationJpaRepository.findByUserIdAndReservationDateAndCancelledAtIsNull(userId, date));
    }

    private static List<Reservation> toDomain(List<ReservationJpaEntity> entities) {
        return entities.stream().map(ReservationPersistenceMapper::toDomain).toList();
    }

    @Override
    public boolean existsOverlappingForUser(UUID userId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return reservationJpaRepository.existsOverlappingForUser(userId, date, startTime, endTime);
    }

    @Override
    public long countActiveByUserSpaceAndDate(UUID userId, UUID spaceId, LocalDate date) {
        return reservationJpaRepository.countActiveByUserSpaceAndDate(userId, spaceId, date);
    }

    @Override
    public List<Reservation> findActiveByBlock(UUID spaceId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return reservationJpaRepository.findActiveByBlock(spaceId, date, startTime, endTime).stream()
                .map(ReservationPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Reservation> findActiveBySpaceId(UUID spaceId) {
        return reservationJpaRepository.findBySpaceIdAndCancelledAtIsNull(spaceId).stream()
                .map(ReservationPersistenceMapper::toDomain)
                .toList();
    }
}
