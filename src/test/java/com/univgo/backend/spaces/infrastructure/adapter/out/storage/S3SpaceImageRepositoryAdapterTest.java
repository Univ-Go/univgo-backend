package com.univgo.backend.spaces.infrastructure.adapter.out.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.univgo.backend.shared.config.S3Properties;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3SpaceImageRepositoryAdapterTest {

    private static final UUID SPACE_ID = UUID.fromString("9dabc028-466a-4ac0-8693-5c78b4b59929");
    private static final S3Properties PROPERTIES =
            new S3Properties("https://storage.example.com", "us-east-2", "key-id", "secret", "univgo-space-images");

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private PresignedGetObjectRequest presignedRequest;

    private S3SpaceImageRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new S3SpaceImageRepositoryAdapter(s3Client, s3Presigner, PROPERTIES);
        // Every presign request signs to a URL that echoes the key, so assertions can tell which
        // object produced which URL without depending on the real signing algorithm.
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenAnswer(invocation -> {
            GetObjectPresignRequest request = invocation.getArgument(0);
            String key = request.getObjectRequest().key();
            when(presignedRequest.url())
                    .thenReturn(URI.create(PROPERTIES.endpoint() + "/" + key).toURL());
            return presignedRequest;
        });
    }

    @Test
    void ordersASpacesPhotographsCoverFirstByUploadTime() {
        givenBucketContains(
                object("spaces/" + SPACE_ID + "/newer.webp", Instant.parse("2026-02-01T00:00:00Z")),
                object("spaces/" + SPACE_ID + "/older.webp", Instant.parse("2026-01-01T00:00:00Z")));

        Map<UUID, List<String>> result = adapter.findAllImageUrls();

        assertThat(result.get(SPACE_ID)).containsExactly(
                "https://storage.example.com/spaces/" + SPACE_ID + "/older.webp",
                "https://storage.example.com/spaces/" + SPACE_ID + "/newer.webp");
    }

    @Test
    void groupsPhotographsBySpaceId() {
        UUID otherSpace = UUID.randomUUID();
        givenBucketContains(
                object("spaces/" + SPACE_ID + "/a.webp", Instant.parse("2026-01-01T00:00:00Z")),
                object("spaces/" + otherSpace + "/b.webp", Instant.parse("2026-01-01T00:00:00Z")));

        Map<UUID, List<String>> result = adapter.findAllImageUrls();

        assertThat(result).containsOnlyKeys(SPACE_ID, otherSpace);
    }

    @Test
    void reusesTheSamePresignedUrlsInsteadOfResigningOnEveryCall() {
        givenBucketContains(object("spaces/" + SPACE_ID + "/a.webp", Instant.parse("2026-01-01T00:00:00Z")));

        Map<UUID, List<String>> first = adapter.findAllImageUrls();
        Map<UUID, List<String>> second = adapter.findAllImageUrls();

        assertThat(second).isEqualTo(first);
        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
    }

    @Test
    void ignoresKeysThatAreNotASpacePhotograph() {
        givenBucketContains(
                object("spaces/not-a-uuid/a.webp", Instant.now()),
                object("spaces/" + SPACE_ID + "/notes.txt", Instant.now()));

        Map<UUID, List<String>> result = adapter.findAllImageUrls();

        assertThat(result).isEmpty();
    }

    private void givenBucketContains(S3Object... objects) {
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenReturn(ListObjectsV2Response.builder().contents(objects).build());
    }

    private static S3Object object(String key, Instant lastModified) {
        return S3Object.builder().key(key).lastModified(lastModified).build();
    }
}
