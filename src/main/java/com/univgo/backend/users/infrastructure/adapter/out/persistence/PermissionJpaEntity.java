package com.univgo.backend.users.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "permissions")
public class PermissionJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    protected PermissionJpaEntity() {
    }

    public PermissionJpaEntity(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
