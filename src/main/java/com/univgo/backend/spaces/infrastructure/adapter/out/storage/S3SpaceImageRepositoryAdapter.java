package com.univgo.backend.spaces.infrastructure.adapter.out.storage;

import com.univgo.backend.shared.config.S3Properties;
import com.univgo.backend.spaces.application.port.out.SpaceImageRepositoryPort;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/**
 * Images are uploaded to {@code spaces/{spaceId}/{file}} by hand — there is no upload endpoint —
 * so there is no row in the database to read: the bucket itself is the source of truth, and this
 * adapter lists it. Presigning is local URL-signing, not a network call, so doing it once per key
 * after a single list costs nothing extra.
 *
 * <p>One {@code ListObjectsV2} call for the whole {@code spaces/} prefix, not one per space, for
 * the same reason {@code GetSpaceCatalogService} batches schedules and reservations instead of
 * asking per space. A single page (up to 1000 keys) covers the catalogue's real scale; move to
 * {@code listObjectsV2Paginator} if the bucket ever needs more than one.
 *
 * <p>Re-signing on every call defeated caching everywhere downstream: SigV4 bakes the request
 * timestamp into the signature, so the same photograph got a different URL on every catalogue
 * read, and neither the browser nor a CDN in front of it can cache a URL that never repeats. The
 * listing and the presigned URLs it produces are cached in memory for {@link #CACHE_DURATION},
 * comfortably inside {@link #PRESIGN_DURATION}, so the same photograph answers with the same URL
 * for as long as that URL is still good — and one process listing the bucket is enough; a bounded
 * cache is not worth the coordination cost since the list is small and rebuilds cheaply.
 */
@Component
public class S3SpaceImageRepositoryAdapter implements SpaceImageRepositoryPort {

    private static final String PREFIX = "spaces/";
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");
    // Long enough to outlive a browsing session on the catalogue, short enough that a link copied
    // out of the app goes stale on its own rather than staying valid forever.
    private static final Duration PRESIGN_DURATION = Duration.ofMinutes(60);
    // Refreshed a margin before the signature actually expires, so a request never serves a URL
    // that goes stale seconds after it reaches the browser.
    private static final Duration CACHE_DURATION = PRESIGN_DURATION.minusMinutes(5);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties properties;

    private volatile CachedUrls cache;

    public S3SpaceImageRepositoryAdapter(S3Client s3Client, S3Presigner s3Presigner, S3Properties properties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.properties = properties;
    }

    @Override
    public Map<UUID, List<String>> findAllImageUrls() {
        CachedUrls cached = cache;

        if (cached != null && cached.isValidAt(Instant.now())) {
            return cached.urls();
        }

        synchronized (this) {
            cached = cache;

            if (cached != null && cached.isValidAt(Instant.now())) {
                return cached.urls();
            }

            Map<UUID, List<String>> fresh = listAndPresign();
            cache = new CachedUrls(fresh, Instant.now().plus(CACHE_DURATION));

            return fresh;
        }
    }

    private Map<UUID, List<String>> listAndPresign() {
        ListObjectsV2Request request =
                ListObjectsV2Request.builder().bucket(properties.bucket()).prefix(PREFIX).build();

        return s3Client.listObjectsV2(request).contents().stream()
                .filter(S3SpaceImageRepositoryAdapter::isImage)
                .sorted(Comparator.comparing(S3Object::lastModified))
                .flatMap(object -> spaceIdOf(object.key()).map(id -> new ImageKey(id, object)).stream())
                .collect(Collectors.groupingBy(
                        ImageKey::spaceId,
                        LinkedHashMap::new,
                        Collectors.mapping(imageKey -> presign(imageKey.object().key()), Collectors.toList())));
    }

    private static boolean isImage(S3Object object) {
        String key = object.key().toLowerCase();
        return IMAGE_EXTENSIONS.stream().anyMatch(key::endsWith);
    }

    /** Key shape is {@code spaces/{spaceId}/{file}}; anything else is not a space photograph. */
    private static Optional<UUID> spaceIdOf(String key) {
        String[] parts = key.split("/", 3);
        if (parts.length < 3) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(parts[1]));
        } catch (IllegalArgumentException notAUuid) {
            return Optional.empty();
        }
    }

    private String presign(String key) {
        GetObjectRequest getRequest =
                GetObjectRequest.builder().bucket(properties.bucket()).key(key).build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(PRESIGN_DURATION)
                .getObjectRequest(getRequest)
                .build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    private record ImageKey(UUID spaceId, S3Object object) {
    }

    private record CachedUrls(Map<UUID, List<String>> urls, Instant expiresAt) {
        boolean isValidAt(Instant now) {
            return now.isBefore(expiresAt);
        }
    }
}
