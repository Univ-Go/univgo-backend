package com.univgo.backend.spaces.application.port.out;

import com.univgo.backend.spaces.domain.Space;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpaceRepositoryPort {

    /**
     * Whether a space exists and is still listed. Reserving, closing or scanning an archived space
     * has to fail the same way as one that never existed, so there is no unfiltered variant: every
     * caller that guards an action wants this one.
     */
    boolean existsActiveById(UUID id);

    /**
     * Answers for an archived space too, on purpose. A student holding a reservation for a space
     * that was just retired still opens its detail, and "My reservations" reads the name, the
     * location and the rules from there. The listing is what hides it.
     */
    Optional<Space> findById(UUID id);

    /**
     * The space, only while it is still listed. What every action that *uses* a space reads —
     * booking it, asking its availability — because those must refuse an archived one outright.
     * Archiving also opens an indefinite closure, which would refuse them anyway, but a closure can
     * be reverted and the archiving cannot be undone by reverting it: the two are not the same
     * guarantee.
     */
    Optional<Space> findActiveById(UUID id);

    /** The catalogue's read: everything a student may still be shown. */
    List<Space> findAllActive();

    /** Inserts a space or replaces its row whole. */
    Space save(Space space);

    void archive(UUID spaceId, UUID actor, LocalDateTime at);

    void restore(UUID spaceId);
}
