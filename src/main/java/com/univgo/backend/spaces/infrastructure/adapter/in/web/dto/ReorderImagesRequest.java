package com.univgo.backend.spaces.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/** Must list every photograph of the space exactly once: a partial order leaves holes. */
public record ReorderImagesRequest(@NotEmpty List<@NotNull UUID> imageIds) {
}
