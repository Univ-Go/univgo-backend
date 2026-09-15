package com.univgo.backend.reservations.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "institution_config")
public class InstitutionConfigJpaEntity {

    @Id
    private short id;

    @Column(name = "block_duration_minutes", nullable = false)
    private int blockDurationMinutes;

    @Column(name = "check_in_tolerance_minutes", nullable = false)
    private int checkInToleranceMinutes;

    @Column(name = "min_usage_minutes", nullable = false)
    private int minUsageMinutes;

    @Column(name = "reservations_per_space_per_day", nullable = false)
    private int reservationsPerSpacePerDay;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected InstitutionConfigJpaEntity() {
    }

    public InstitutionConfigJpaEntity(
            int blockDurationMinutes,
            int checkInToleranceMinutes,
            int minUsageMinutes,
            int reservationsPerSpacePerDay,
            LocalDateTime updatedAt) {
        this.id = 1;
        this.blockDurationMinutes = blockDurationMinutes;
        this.checkInToleranceMinutes = checkInToleranceMinutes;
        this.minUsageMinutes = minUsageMinutes;
        this.reservationsPerSpacePerDay = reservationsPerSpacePerDay;
        this.updatedAt = updatedAt;
    }

    public int getBlockDurationMinutes() {
        return blockDurationMinutes;
    }

    public int getCheckInToleranceMinutes() {
        return checkInToleranceMinutes;
    }

    public int getMinUsageMinutes() {
        return minUsageMinutes;
    }

    public int getReservationsPerSpacePerDay() {
        return reservationsPerSpacePerDay;
    }
}
