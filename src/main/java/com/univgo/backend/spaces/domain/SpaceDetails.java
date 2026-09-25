package com.univgo.backend.spaces.domain;

import java.util.List;

/**
 * Informational content shown to a student about a space: what it is, and what to read before
 * booking it. Kept apart from the fields booking logic actually reasons over.
 */
public record SpaceDetails(String description, List<String> rules) {

    public SpaceDetails {
        rules = List.copyOf(rules);
    }
}
