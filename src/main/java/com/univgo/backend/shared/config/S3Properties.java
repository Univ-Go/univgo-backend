package com.univgo.backend.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Credentials and bucket for the S3-compatible object storage that holds space photographs.
 * Photographs are uploaded to the bucket by hand — there is no upload endpoint — so this only
 * needs enough to list a space's keys and sign them for reading.
 *
 * @param endpoint         S3-compatible endpoint; the bucket is not AWS, so this must be explicit.
 * @param region           region the bucket reports itself in.
 * @param accessKeyId      access key for a principal scoped to this bucket.
 * @param secretAccessKey  secret for that key.
 * @param bucket           bucket name.
 */
@ConfigurationProperties("aws.s3")
public record S3Properties(
        String endpoint,
        String region,
        String accessKeyId,
        String secretAccessKey,
        String bucket) {
}
