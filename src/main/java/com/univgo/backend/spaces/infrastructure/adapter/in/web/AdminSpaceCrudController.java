package com.univgo.backend.spaces.infrastructure.adapter.in.web;

import com.univgo.backend.spaces.application.port.in.AddSpaceImagesUseCase;
import com.univgo.backend.spaces.application.port.in.ArchiveSpaceUseCase;
import com.univgo.backend.spaces.application.port.in.CreateSpaceUseCase;
import com.univgo.backend.spaces.application.port.in.CreateSpaceUseCase.CreateSpaceCommand;
import com.univgo.backend.spaces.application.port.in.DeleteSpaceImageUseCase;
import com.univgo.backend.spaces.application.port.in.GetAdminSpacesUseCase;
import com.univgo.backend.spaces.application.port.in.ReorderSpaceImagesUseCase;
import com.univgo.backend.spaces.application.port.in.ReplaceSpaceSchedulesUseCase;
import com.univgo.backend.spaces.application.port.in.RestoreSpaceUseCase;
import com.univgo.backend.spaces.application.port.in.UpdateSpaceIdentityUseCase;
import com.univgo.backend.spaces.application.port.in.UpdateSpaceIdentityUseCase.UpdateSpaceIdentityCommand;
import com.univgo.backend.spaces.application.port.in.UpdateSpaceUsageUseCase;
import com.univgo.backend.spaces.application.port.in.UpdateSpaceUsageUseCase.UpdateSpaceUsageCommand;
import com.univgo.backend.spaces.application.port.out.ImageUpload;
import com.univgo.backend.spaces.infrastructure.adapter.in.web.dto.AdminSpaceDetailResponse;
import com.univgo.backend.spaces.infrastructure.adapter.in.web.dto.AdminSpaceSummaryResponse;
import com.univgo.backend.spaces.infrastructure.adapter.in.web.dto.ArchiveSpaceResponse;
import com.univgo.backend.spaces.infrastructure.adapter.in.web.dto.CreateSpaceRequest;
import com.univgo.backend.spaces.infrastructure.adapter.in.web.dto.ReorderImagesRequest;
import com.univgo.backend.spaces.infrastructure.adapter.in.web.dto.ReplaceSchedulesRequest;
import com.univgo.backend.spaces.infrastructure.adapter.in.web.dto.ScheduleWindowRequest;
import com.univgo.backend.spaces.infrastructure.adapter.in.web.dto.ScheduleWindowResponse;
import com.univgo.backend.spaces.infrastructure.adapter.in.web.dto.SpaceImageResponse;
import com.univgo.backend.spaces.infrastructure.adapter.in.web.dto.UpdateSpaceIdentityRequest;
import com.univgo.backend.spaces.infrastructure.adapter.in.web.dto.UpdateSpaceUsageRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Managing the catalogue of spaces. It shares the {@code /admin/spaces} base path with the
 * reservations module's {@code AdminSpacesController}, which answers about a space's blocks and its
 * closures: the patterns are distinct, so nothing is ambiguous, and the split follows what each
 * endpoint is about rather than what it is addressed as.
 *
 * <p>This is the {@code spaces} module's first inbound adapter. Writing a space's description, its
 * hours and its photographs is not a question about reservations, so it does not belong behind that
 * module's controllers.
 */
@RestController
@RequestMapping("/admin/spaces")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSpaceCrudController {

    private final GetAdminSpacesUseCase getAdminSpacesUseCase;
    private final CreateSpaceUseCase createSpaceUseCase;
    private final UpdateSpaceIdentityUseCase updateSpaceIdentityUseCase;
    private final UpdateSpaceUsageUseCase updateSpaceUsageUseCase;
    private final ReplaceSpaceSchedulesUseCase replaceSpaceSchedulesUseCase;
    private final AddSpaceImagesUseCase addSpaceImagesUseCase;
    private final DeleteSpaceImageUseCase deleteSpaceImageUseCase;
    private final ReorderSpaceImagesUseCase reorderSpaceImagesUseCase;
    private final ArchiveSpaceUseCase archiveSpaceUseCase;
    private final RestoreSpaceUseCase restoreSpaceUseCase;

    public AdminSpaceCrudController(
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
        this.getAdminSpacesUseCase = getAdminSpacesUseCase;
        this.createSpaceUseCase = createSpaceUseCase;
        this.updateSpaceIdentityUseCase = updateSpaceIdentityUseCase;
        this.updateSpaceUsageUseCase = updateSpaceUsageUseCase;
        this.replaceSpaceSchedulesUseCase = replaceSpaceSchedulesUseCase;
        this.addSpaceImagesUseCase = addSpaceImagesUseCase;
        this.deleteSpaceImageUseCase = deleteSpaceImageUseCase;
        this.reorderSpaceImagesUseCase = reorderSpaceImagesUseCase;
        this.archiveSpaceUseCase = archiveSpaceUseCase;
        this.restoreSpaceUseCase = restoreSpaceUseCase;
    }

    @GetMapping
    public List<AdminSpaceSummaryResponse> list(@RequestParam(defaultValue = "false") boolean includeArchived) {
        return getAdminSpacesUseCase.list(includeArchived).stream()
                .map(AdminSpaceSummaryResponse::from)
                .toList();
    }

    @GetMapping("/{spaceId}")
    public AdminSpaceDetailResponse detail(@PathVariable UUID spaceId) {
        return AdminSpaceDetailResponse.from(getAdminSpacesUseCase.detail(spaceId));
    }

    /**
     * One request for the whole space. The part order of {@code photos} is the photograph order, so
     * the first file the wizard appended becomes the cover.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdminSpaceDetailResponse> create(
            @Valid @RequestPart("space") CreateSpaceRequest request,
            @RequestPart("photos") List<MultipartFile> photos,
            Authentication authentication) {
        CreateSpaceCommand command = new CreateSpaceCommand(
                request.name(),
                request.location(),
                request.spaceTypeId(),
                request.capacity(),
                request.description(),
                request.safeRules(),
                request.schedules().stream().map(ScheduleWindowRequest::toCommand).toList(),
                toUploads(photos),
                actorOf(authentication));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AdminSpaceDetailResponse.from(createSpaceUseCase.execute(command)));
    }

    @PutMapping("/{spaceId}/identity")
    public ResponseEntity<Void> updateIdentity(
            @PathVariable UUID spaceId, @Valid @RequestBody UpdateSpaceIdentityRequest request) {
        updateSpaceIdentityUseCase.execute(new UpdateSpaceIdentityCommand(
                spaceId, request.name(), request.location(), request.spaceTypeId(), request.capacity()));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{spaceId}/usage")
    public ResponseEntity<Void> updateUsage(
            @PathVariable UUID spaceId, @Valid @RequestBody UpdateSpaceUsageRequest request) {
        updateSpaceUsageUseCase.execute(new UpdateSpaceUsageCommand(spaceId, request.description(), request.safeRules()));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{spaceId}/schedules")
    public List<ScheduleWindowResponse> replaceSchedules(
            @PathVariable UUID spaceId, @Valid @RequestBody ReplaceSchedulesRequest request) {
        return replaceSpaceSchedulesUseCase
                .execute(spaceId, request.schedules().stream().map(ScheduleWindowRequest::toCommand).toList())
                .stream()
                .map(ScheduleWindowResponse::from)
                .toList();
    }

    @PostMapping(path = "/{spaceId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<SpaceImageResponse>> addImages(
            @PathVariable UUID spaceId,
            @RequestPart("photos") List<MultipartFile> photos,
            Authentication authentication) {
        List<SpaceImageResponse> images = addSpaceImagesUseCase
                .execute(spaceId, toUploads(photos), actorOf(authentication))
                .stream()
                .map(SpaceImageResponse::from)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(images);
    }

    @DeleteMapping("/{spaceId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID spaceId, @PathVariable UUID imageId) {
        deleteSpaceImageUseCase.execute(spaceId, imageId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{spaceId}/images/order")
    public List<SpaceImageResponse> reorderImages(
            @PathVariable UUID spaceId, @Valid @RequestBody ReorderImagesRequest request) {
        return reorderSpaceImagesUseCase.execute(spaceId, request.imageIds()).stream()
                .map(SpaceImageResponse::from)
                .toList();
    }

    @PostMapping("/{spaceId}/archive")
    public ArchiveSpaceResponse archive(@PathVariable UUID spaceId, Authentication authentication) {
        return new ArchiveSpaceResponse(archiveSpaceUseCase.execute(spaceId, actorOf(authentication)));
    }

    @PostMapping("/{spaceId}/restore")
    public ResponseEntity<Void> restore(@PathVariable UUID spaceId) {
        restoreSpaceUseCase.execute(spaceId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reads each part into memory here, so {@code MultipartFile} never reaches a use case. Spring
     * has already spilled anything over the configured threshold to disk, and the per-file ceiling
     * in {@code application.yml} is what bounds this.
     */
    private static List<ImageUpload> toUploads(List<MultipartFile> photos) {
        return photos.stream().map(AdminSpaceCrudController::toUpload).toList();
    }

    private static ImageUpload toUpload(MultipartFile photo) {
        try {
            return new ImageUpload(photo.getOriginalFilename(), photo.getContentType(), photo.getBytes());
        } catch (IOException cannotRead) {
            throw new UncheckedIOException(cannotRead);
        }
    }

    private static UUID actorOf(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
