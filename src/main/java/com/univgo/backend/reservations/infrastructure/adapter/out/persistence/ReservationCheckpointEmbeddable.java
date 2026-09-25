package com.univgo.backend.reservations.infrastructure.adapter.out.persistence;

import com.univgo.backend.reservations.domain.CancelledBy;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable
class ReservationCheckpointEmbeddable {

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Convert(converter = CancelledByConverter.class)
    @Column(name = "cancelled_by")
    private CancelledBy cancelledBy;

    protected ReservationCheckpointEmbeddable() {
    }

    ReservationCheckpointEmbeddable(LocalDateTime checkedInAt, LocalDateTime cancelledAt, CancelledBy cancelledBy) {
        this.checkedInAt = checkedInAt;
        this.cancelledAt = cancelledAt;
        this.cancelledBy = cancelledBy;
    }

    LocalDateTime getCheckedInAt() {
        return checkedInAt;
    }

    LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    CancelledBy getCancelledBy() {
        return cancelledBy;
    }
}
