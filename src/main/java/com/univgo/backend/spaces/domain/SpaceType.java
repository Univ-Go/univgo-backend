package com.univgo.backend.spaces.domain;

import java.util.UUID;

/**
 * An entry in the taxonomy a space points at. The category is not a property of the space itself —
 * two sports courts share a taxonomy, not a set of instructions — so choosing a type is how a space
 * acquires its category.
 *
 * <p>The product lists types so an administrator can pick one; it does not edit them. The pilot has
 * only sports spaces, and classrooms and laboratories are what the remaining rows are for.
 */
public record SpaceType(UUID id, String name, SpaceCategory category) {
}
