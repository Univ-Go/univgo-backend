package com.univgo.backend.spaces.domain;

/**
 * The coarse grouping the catalog lists spaces under. It is a property of the space type, not of
 * the space: a court and a study room differ in kind, two courts do not.
 */
public enum SpaceCategory {
    SPORTS,
    STUDY,
    LAB
}
