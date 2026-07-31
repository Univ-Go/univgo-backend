package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "spaces")
public class SpaceJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "space_type_id", nullable = false)
    private UUID spaceTypeId;

    protected SpaceJpaEntity() {
    }
}
