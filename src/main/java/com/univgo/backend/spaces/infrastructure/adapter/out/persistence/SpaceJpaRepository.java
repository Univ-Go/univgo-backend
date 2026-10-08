package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpaceJpaRepository extends JpaRepository<SpaceJpaEntity, UUID> {

    /** Fetched with the type so listing the catalog stays one query instead of one per space. */
    @Override
    @Query("select s from SpaceJpaEntity s join fetch s.spaceType")
    List<SpaceJpaEntity> findAll();

    /**
     * What every catalogue read wants. An archived space still has rows — its reservations are
     * history — so it is excluded here rather than deleted.
     */
    @Query("select s from SpaceJpaEntity s join fetch s.spaceType where s.archivedAt is null order by s.name asc")
    List<SpaceJpaEntity> findAllActive();

    @Query("select s from SpaceJpaEntity s join fetch s.spaceType where s.id = :id and s.archivedAt is null")
    Optional<SpaceJpaEntity> findActiveById(@Param("id") UUID id);

    @Query("select count(s) > 0 from SpaceJpaEntity s where s.id = :id and s.archivedAt is null")
    boolean existsActiveById(@Param("id") UUID id);

    /**
     * The panel's own reads. Ordered by name because the grid is a list a person scans, and
     * including archived spaces is what makes restoring one possible without SQL.
     */
    @Query("select s from SpaceJpaEntity s join fetch s.spaceType order by s.name asc")
    List<SpaceJpaEntity> findAllForAdmin();

    @Query("select s from SpaceJpaEntity s join fetch s.spaceType where s.id = :id")
    Optional<SpaceJpaEntity> findByIdForAdmin(@Param("id") UUID id);

    @Modifying
    @Query("update SpaceJpaEntity s set s.archivedAt = :at, s.archivedBy = :actor where s.id = :id")
    int archive(@Param("id") UUID id, @Param("at") LocalDateTime at, @Param("actor") UUID actor);

    @Modifying
    @Query("update SpaceJpaEntity s set s.archivedAt = null, s.archivedBy = null where s.id = :id")
    int restore(@Param("id") UUID id);
}
