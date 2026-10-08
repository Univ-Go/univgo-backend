package com.univgo.backend.spaces.application.port.out;

import com.univgo.backend.spaces.domain.SpaceSchedule;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SpaceScheduleRepositoryPort {

    List<SpaceSchedule> findBySpaceIdAndDayOfWeek(UUID spaceId, int dayOfWeek);

    /** Every space's windows for one weekday, so listing the catalog does not ask space by space. */
    List<SpaceSchedule> findByDayOfWeek(int dayOfWeek);

    /** Every space's whole week, keyed by space: the panel's grid counts windows per space. */
    Map<UUID, List<SpaceSchedule>> findAllBySpaceIds();

    /** One space's whole week, which is what the admin panel edits. */
    List<SpaceSchedule> findBySpaceId(UUID spaceId);

    /**
     * Replaces a space's whole week. Not per-window CRUD: {@code V17} already set that contract,
     * and deleting before inserting inside one transaction sidesteps the uniqueness constraint
     * {@code V16} added — a partial update would have to order its writes so as not to collide
     * transiently with a row it is about to remove.
     */
    void replaceAll(UUID spaceId, List<SpaceSchedule> windows);
}
