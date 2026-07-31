package com.univgo.backend.reservations.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "reservation_guests")
public class ReservationGuestJpaEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private ReservationJpaEntity reservation;

    @Column(name = "guest_identification", nullable = false)
    private String guestIdentification;

    @Column(name = "guest_name")
    private String guestName;

    protected ReservationGuestJpaEntity() {
    }

    public ReservationGuestJpaEntity(UUID id, ReservationJpaEntity reservation, String guestIdentification, String guestName) {
        this.id = id;
        this.reservation = reservation;
        this.guestIdentification = guestIdentification;
        this.guestName = guestName;
    }

    public UUID getId() {
        return id;
    }

    public String getGuestIdentification() {
        return guestIdentification;
    }

    public String getGuestName() {
        return guestName;
    }
}
