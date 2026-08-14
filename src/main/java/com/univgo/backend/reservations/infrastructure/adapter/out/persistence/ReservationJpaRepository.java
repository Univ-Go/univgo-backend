package com.univgo.backend.reservations.infrastructure.adapter.out.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationJpaRepository extends JpaRepository<ReservationJpaEntity, UUID> {

    List<ReservationJpaEntity> findByUserId(UUID userId);

    @Query("""
            select case when count(r) > 0 then true else false end
            from ReservationJpaEntity r
            where r.spaceId = :spaceId
              and r.reservationDate = :reservationDate
              and r.status not in (com.univgo.backend.reservations.domain.ReservationStatus.CANCELLED_BY_ADMIN,
                                    com.univgo.backend.reservations.domain.ReservationStatus.CANCELLED_BY_USER)
              and r.startTime < :endTime
              and r.endTime > :startTime
            """)
    boolean existsOverlapping(
            @Param("spaceId") UUID spaceId,
            @Param("reservationDate") LocalDate reservationDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
}
