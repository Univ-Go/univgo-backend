package com.univgo.backend.reservations.domain;

import java.util.UUID;

/** What the "elegir el espacio" catalog screen shows per space. */
public record SpaceCatalogItem(UUID spaceId, String name, int capacity, boolean underMaintenance, boolean hasFreeBlockToday) {
}
