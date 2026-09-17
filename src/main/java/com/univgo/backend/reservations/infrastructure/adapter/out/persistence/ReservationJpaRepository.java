package com.univgo.backend.reservations.infrastructure.adapter.out.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationJpaRepository extends JpaRepository<ReservationJpaEntity, UUID> {

    List<ReservationJpaEntity> findByUserId(UUID userId);

    /** Cross-space: the same student can't be in two places at once (spec: "nunca dos reservas que se solapen"). */
    @Query("""
            select case when count(r) > 0 then true else false end
            from ReservationJpaEntity r
            where r.userId = :userId
              and r.reservationDate = :reservationDate
              and r.cancelledAt is null
              and r.startTime < :endTime
              and r.endTime > :startTime
            """)
    boolean existsOverlappingForUser(
            @Param("userId") UUID userId,
            @Param("reservationDate") LocalDate reservationDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    /** Cancelled reservations don't count against the daily limit; expired/finished ones still do (spec: gasta la reserva del día). */
    @Query("""
            select count(r)
            from ReservationJpaEntity r
            where r.userId = :userId
              and r.spaceId = :spaceId
              and r.reservationDate = :reservationDate
              and r.cancelledAt is null
            """)
    long countActiveByUserSpaceAndDate(
            @Param("userId") UUID userId, @Param("spaceId") UUID spaceId, @Param("reservationDate") LocalDate reservationDate);

    @Query("""
            select r
            from ReservationJpaEntity r
            where r.spaceId = :spaceId
              and r.reservationDate = :reservationDate
              and r.startTime = :startTime
              and r.endTime = :endTime
              and r.cancelledAt is null
            """)
    List<ReservationJpaEntity> findActiveByBlock(
            @Param("spaceId") UUID spaceId,
            @Param("reservationDate") LocalDate reservationDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    List<ReservationJpaEntity> findByReservationDateAndCancelledAtIsNull(LocalDate reservationDate);

    List<ReservationJpaEntity> findBySpaceIdAndReservationDateAndCancelledAtIsNull(UUID spaceId, LocalDate reservationDate);

    List<ReservationJpaEntity> findByUserIdAndReservationDateAndCancelledAtIsNull(UUID userId, LocalDate reservationDate);

    Optional<ReservationJpaEntity> findByQrCodeData(String qrCodeData);

    List<ReservationJpaEntity> findBySpaceIdAndCancelledAtIsNull(UUID spaceId);
}
