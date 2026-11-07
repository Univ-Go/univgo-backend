package com.univgo.backend.spaces.infrastructure.adapter.in.web;

import com.univgo.backend.spaces.application.port.in.AddSpaceImagesUseCase;
import com.univgo.backend.spaces.application.port.in.ArchiveSpaceUseCase;
import com.univgo.backend.spaces.application.port.in.CreateSpaceUseCase;
import com.univgo.backend.spaces.application.port.in.DeleteSpaceImageUseCase;
import com.univgo.backend.spaces.application.port.in.GetAdminSpacesUseCase;
import com.univgo.backend.spaces.application.port.in.ReorderSpaceImagesUseCase;
import com.univgo.backend.spaces.application.port.in.ReplaceSpaceSchedulesUseCase;
import com.univgo.backend.spaces.application.port.in.RestoreSpaceUseCase;
import com.univgo.backend.spaces.application.port.in.UpdateSpaceIdentityUseCase;
import com.univgo.backend.spaces.application.port.in.UpdateSpaceUsageUseCase;
import org.springframework.stereotype.Component;

/**
 * Bundles the in-ports {@link AdminSpaceCrudController} depends on into one injectable bean, so
 * the controller's constructor takes one parameter instead of one per use case.
 */
@Component
public record AdminSpaceCrudUseCases(
        GetAdminSpacesUseCase getAdminSpacesUseCase,
        CreateSpaceUseCase createSpaceUseCase,
        UpdateSpaceIdentityUseCase updateSpaceIdentityUseCase,
        UpdateSpaceUsageUseCase updateSpaceUsageUseCase,
        ReplaceSpaceSchedulesUseCase replaceSpaceSchedulesUseCase,
        AddSpaceImagesUseCase addSpaceImagesUseCase,
        DeleteSpaceImageUseCase deleteSpaceImageUseCase,
        ReorderSpaceImagesUseCase reorderSpaceImagesUseCase,
        ArchiveSpaceUseCase archiveSpaceUseCase,
        RestoreSpaceUseCase restoreSpaceUseCase) {
}
