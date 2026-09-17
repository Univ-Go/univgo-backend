package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpaceJpaRepository extends JpaRepository<SpaceJpaEntity, UUID> {

    /** Fetched with the type so listing the catalog stays one query instead of one per space. */
    @Override
    @Query("select s from SpaceJpaEntity s join fetch s.spaceType")
    List<SpaceJpaEntity> findAll();
}
