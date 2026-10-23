package com.univgo.backend.spaces.domain;

/**
 * One row of the panel's space grid. The counts are what let the grid say a space is missing its
 * hours or its photographs without opening it.
 */
public record AdminSpaceSummary(
        AdminSpaceView space, int scheduleWindowCount, int imageCount, String coverImageUrl) {
}
