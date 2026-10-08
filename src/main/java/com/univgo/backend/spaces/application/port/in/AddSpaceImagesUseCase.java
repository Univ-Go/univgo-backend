package com.univgo.backend.spaces.application.port.in;

import com.univgo.backend.spaces.application.port.out.ImageUpload;
import com.univgo.backend.spaces.domain.SpaceImageView;
import java.util.List;
import java.util.UUID;

public interface AddSpaceImagesUseCase {

    /** Appends at the end of the existing order and returns the space's whole list. */
    List<SpaceImageView> execute(UUID spaceId, List<ImageUpload> photos, UUID actor);
}
