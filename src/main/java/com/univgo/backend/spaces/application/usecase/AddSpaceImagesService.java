package com.univgo.backend.spaces.application.usecase;

import com.univgo.backend.spaces.application.port.in.AddSpaceImagesUseCase;
import com.univgo.backend.spaces.application.port.in.GetSpaceImagesUseCase;
import com.univgo.backend.spaces.application.port.out.ImageUpload;
import com.univgo.backend.spaces.application.port.out.SpaceImageRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.domain.SpaceImageView;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddSpaceImagesService implements AddSpaceImagesUseCase {

    private final SpaceRepositoryPort spaceRepositoryPort;
    private final SpaceImageRepositoryPort spaceImageRepositoryPort;
    private final SpaceImageWriter spaceImageWriter;
    private final GetSpaceImagesUseCase getSpaceImagesUseCase;

    public AddSpaceImagesService(
            SpaceRepositoryPort spaceRepositoryPort,
            SpaceImageRepositoryPort spaceImageRepositoryPort,
            SpaceImageWriter spaceImageWriter,
            GetSpaceImagesUseCase getSpaceImagesUseCase) {
        this.spaceRepositoryPort = spaceRepositoryPort;
        this.spaceImageRepositoryPort = spaceImageRepositoryPort;
        this.spaceImageWriter = spaceImageWriter;
        this.getSpaceImagesUseCase = getSpaceImagesUseCase;
    }

    @Override
    @Transactional
    public List<SpaceImageView> execute(UUID spaceId, List<ImageUpload> photos, UUID actor) {
        if (spaceRepositoryPort.findById(spaceId).isEmpty()) {
            throw new SpaceNotFoundException(spaceId);
        }
        if (photos.isEmpty()) {
            throw new IllegalArgumentException("No photographs were uploaded");
        }

        int existing = spaceImageRepositoryPort.countBySpaceId(spaceId);
        spaceImageWriter.guardTotal(existing + photos.size());
        spaceImageWriter.store(spaceId, photos, existing, actor);

        return getSpaceImagesUseCase.ofSpace(spaceId);
    }
}
