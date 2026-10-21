package com.univgo.backend.spaces.infrastructure.adapter.out.storage;

import com.univgo.backend.shared.config.S3Properties;
import com.univgo.backend.shared.config.SpaceImageProperties;
import com.univgo.backend.spaces.application.port.out.SpaceImageStoragePort;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/**
 * The bytes of a space's photographs. What used to live here — listing the whole {@code spaces/}
 * prefix, sorting by {@code lastModified} and caching the signed URLs — is gone: it all existed to
 * stand in for a table that now exists. Order comes from {@code space_images.position}, and this
 * adapter no longer has an opinion about which photographs a space has.
 *
 * <p>Presigning is local URL-signing and not a network call, so signing a key on demand costs
 * nothing. The signature still bakes in a timestamp, so the URL rotates and a browser's cache
 * misses once an hour; the derivatives are small enough that this is cheaper than the alternatives,
 * and no image optimizer sits in front of them any more.
 */
@Component
public class S3SpaceImageStorageAdapter implements SpaceImageStoragePort {

    // Long enough to outlive a browsing session, short enough that a link copied out of the app
    // goes stale on its own rather than staying valid forever.
    private static final Duration PRESIGN_DURATION = Duration.ofMinutes(60);

    private static final String DERIVATIVE_EXTENSION = ".jpg";

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties properties;
    private final SpaceImageProperties imageProperties;

    public S3SpaceImageStorageAdapter(
            S3Client s3Client,
            S3Presigner s3Presigner,
            S3Properties properties,
            SpaceImageProperties imageProperties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.properties = properties;
        this.imageProperties = imageProperties;
    }

    @Override
    public void put(String key, byte[] bytes, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .contentType(contentType)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(bytes));
    }

    @Override
    public void deletePrefix(String keyPrefix) {
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(properties.bucket())
                .prefix(keyPrefix)
                .build();
        List<ObjectIdentifier> keys = s3Client.listObjectsV2(listRequest).contents().stream()
                .map(S3Object::key)
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();

        if (keys.isEmpty()) {
            return;
        }

        s3Client.deleteObjects(DeleteObjectsRequest.builder()
                .bucket(properties.bucket())
                .delete(Delete.builder().objects(keys).build())
                .build());
    }

    @Override
    public Map<Integer, String> urlsOf(String originalKey) {
        return imageProperties.widths().stream()
                .collect(Collectors.toMap(
                        width -> width, width -> presign(derivativeKey(originalKey, width))));
    }

    @Override
    public String derivativeKey(String originalKey, int width) {
        return originalKey + "/" + width + DERIVATIVE_EXTENSION;
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
}
