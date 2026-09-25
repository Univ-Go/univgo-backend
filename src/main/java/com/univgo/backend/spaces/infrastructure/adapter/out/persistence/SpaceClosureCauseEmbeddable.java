package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import com.univgo.backend.spaces.domain.ClosureReason;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;

@Embeddable
class SpaceClosureCauseEmbeddable {

    /** Same arrangement the reservation's own enums use: a converter writes the lower-case label,
     *  and {@code stringtype=unspecified} in the JDBC URL lets Postgres cast it to its enum type. */
    @Convert(converter = ClosureReasonConverter.class)
    @Column(nullable = false)
    private ClosureReason reason;

    @Column
    private String details;

    protected SpaceClosureCauseEmbeddable() {
    }

    SpaceClosureCauseEmbeddable(ClosureReason reason, String details) {
        this.reason = reason;
        this.details = details;
    }

    ClosureReason getReason() {
        return reason;
    }

    String getDetails() {
        return details;
    }
}
