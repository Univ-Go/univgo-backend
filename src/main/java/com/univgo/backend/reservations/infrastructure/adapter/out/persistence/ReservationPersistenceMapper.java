package com.univgo.backend.reservations.infrastructure.adapter.out.persistence;

import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationCheckpoint;
import com.univgo.backend.reservations.domain.ReservationSchedule;
import com.univgo.backend.spaces.domain.TimeBlock;

final class ReservationPersistenceMapper {

    private ReservationPersistenceMapper() {
    }

    static Reservation toDomain(ReservationJpaEntity entity) {
        return new Reservation(
                entity.getId(),
                entity.getQrCodeData(),
                entity.getUserId(),
                entity.getSpaceId(),
                new ReservationSchedule(
                        entity.getReservationDate(), new TimeBlock(entity.getStartTime(), entity.getEndTime())),
                entity.getCreatedAt(),
                new ReservationCheckpoint(entity.getCheckedInAt(), entity.getCancelledAt(), entity.getCancelledBy()));
    }

    static ReservationJpaEntity toEntity(Reservation reservation) {
        return new ReservationJpaEntity(
                reservation.getId(),
                reservation.getQrCodeData(),
                reservation.getUserId(),
                reservation.getSpaceId(),
                new ReservationScheduleEmbeddable(
                        reservation.getReservationDate(), reservation.getBlockStart(), reservation.getBlockEnd()),
                reservation.getCreatedAt(),
                new ReservationCheckpointEmbeddable(
                        reservation.getCheckedInAt(), reservation.getCancelledAt(), reservation.getCancelledBy()));
    }
}
