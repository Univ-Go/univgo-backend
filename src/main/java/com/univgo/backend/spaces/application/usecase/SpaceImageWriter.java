package com.univgo.backend.spaces.application.usecase;

import com.univgo.backend.shared.util.Uuidv7Generator;
import com.univgo.backend.spaces.application.port.out.ImageUpload;
import com.univgo.backend.spaces.application.port.out.ResizedImage;
import com.univgo.backend.spaces.application.port.out.SpaceImageLimitsPort;
import com.univgo.backend.spaces.application.port.out.SpaceImageRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceImageResizerPort;
import com.univgo.backend.spaces.application.port.out.SpaceImageStoragePort;
import com.univgo.backend.spaces.domain.SpaceImage;
import com.univgo.backend.spaces.domain.TooManyImagesException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Turning uploaded files into stored photographs, shared by creating a space and adding to one.
 *
 * <p>The rows are written before the bytes, so a failure to store a derivative rolls the rows back
 * and nothing becomes readable: the database is the source of truth for which photographs a space
 * has, so an object nothing points at is invisible to the application.
 *
 * <p><b>Atomicity across Postgres and S3 does not exist</b>, and this does not pretend otherwise.
 * What it guarantees is that a half-written set is never *visible*. The objects a rolled-back
 * transaction leaves in the bucket are cleaned up on a best-effort basis below — if that cleanup
 * fails too, a few orphaned objects remain, costing storage and nothing else.
 */
@Component
class SpaceImageWriter {

    private final SpaceImageResizerPort resizer;
    private final SpaceImageRepositoryPort repository;
    private final SpaceImageStoragePort storage;
    private final SpaceImageLimitsPort limits;

    SpaceImageWriter(
            SpaceImageResizerPort resizer,
            SpaceImageRepositoryPort repository,
            SpaceImageStoragePort storage,
            SpaceImageLimitsPort limits) {
        this.resizer = resizer;
        this.repository = repository;
        this.storage = storage;
        this.limits = limits;
    }

    void guardTotal(int total) {
        if (total > limits.maxPerSpace()) {
            throw new TooManyImagesException(total, limits.maxPerSpace());
        }
    }

    /**
     * Stores every upload, in the order given, starting at {@code startPosition}.
     *
     * <p>Each photograph is decoded, derived and stored before the next is read, and the derivatives
     * of one are not retained afterwards. A 12 MP source occupies roughly 48 MB decoded, so handling
     * a batch all at once — or in parallel — exhausts a small instance. **Do not parallelise this
     * loop.**
     */
    List<SpaceImage> store(UUID spaceId, List<ImageUpload> photos, int startPosition, UUID actor) {
        List<SpaceImage> stored = new ArrayList<>(photos.size());
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < photos.size(); i++) {
            ImageUpload photo = photos.get(i);
            ResizedImage resized = resizer.resize(photo);
            UUID imageId = Uuidv7Generator.generate();
            String originalKey = SpaceImage.keyFor(spaceId, imageId);

            SpaceImage image = new SpaceImage(
                    imageId,
                    spaceId,
                    startPosition + i,
                    originalKey,
                    resized.contentType(),
                    resized.sourceWidth(),
                    resized.sourceHeight(),
                    totalBytes(resized),
                    now,
                    actor);

            repository.save(image);
            cleanUpIfRolledBack(originalKey);

            for (Map.Entry<Integer, byte[]> derivative : resized.derivatives().entrySet()) {
                storage.put(
                        storage.derivativeKey(originalKey, derivative.getKey()),
                        derivative.getValue(),
                        resized.contentType());
            }

            stored.add(image);
        }

        return stored;
    }

    private static int totalBytes(ResizedImage resized) {
        return resized.derivatives().values().stream().mapToInt(bytes -> bytes.length).sum();
    }

    private void cleanUpIfRolledBack(String originalKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    try {
                        storage.deletePrefix(originalKey);
                    } catch (RuntimeException cleanupFailed) {
                        // Best effort on purpose: the rows are already gone, so what is left is
                        // storage nothing references. Failing here would replace a harmless orphan
                        // with an error the administrator cannot act on.
                    }
                }
            }
        });
    }
}
