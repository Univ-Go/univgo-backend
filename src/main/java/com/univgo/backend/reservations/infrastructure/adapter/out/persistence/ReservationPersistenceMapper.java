package com.univgo.backend.reservations.infrastructure.adapter.out.persistence;

import com.univgo.backend.reservations.domain.Reservation;

final class ReservationPersistenceMapper {

    private ReservationPersistenceMapper() {
    }

    static Reservation toDomain(ReservationJpaEntity entity) {
        return new Reservation(
                entity.getId(),
                entity.getQrCodeData(),
                entity.getUserId(),
                entity.getSpaceId(),
                entity.getReservationDate(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getCreatedAt(),
                entity.getCheckedInAt(),
                entity.getCancelledAt(),
                entity.getCancelledBy());
    }

    static ReservationJpaEntity toEntity(Reservation reservation) {
        return new ReservationJpaEntity(
                reservation.getId(),
                reservation.getQrCodeData(),
                reservation.getUserId(),
                reservation.getSpaceId(),
                reservation.getReservationDate(),
                reservation.getBlockStart(),
                reservation.getBlockEnd(),
                reservation.getCreatedAt(),
                reservation.getCheckedInAt(),
                reservation.getCancelledAt(),
                reservation.getCancelledBy());
    }
}
