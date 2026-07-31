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
    public Reservation save(Reservation reservation) {
        var saved = reservationJpaRepository.save(ReservationPersistenceMapper.toEntity(reservation));
        return ReservationPersistenceMapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        reservationJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsOverlapping(UUID spaceId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return reservationJpaRepository.existsOverlapping(spaceId, date, startTime, endTime);
    }
}
