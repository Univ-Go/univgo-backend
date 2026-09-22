package com.univgo.backend.shared.config;

import java.net.URI;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Path-style addressing ({@code endpoint/bucket}, not {@code bucket.endpoint}) is on because the
 * bucket lives on Neon's S3-compatible storage rather than AWS: virtual-hosted-style would need a
 * DNS wildcard the provider does not promise.
 */
@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

    @Bean
    public S3Client s3Client(S3Properties properties) {
        return S3Client.builder()
                .endpointOverride(URI.create(properties.endpoint()))
                .region(Region.of(properties.region()))
                .credentialsProvider(credentialsOf(properties))
                .serviceConfiguration(pathStyleAccess())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(S3Properties properties) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(properties.endpoint()))
                .region(Region.of(properties.region()))
                .credentialsProvider(credentialsOf(properties))
                .serviceConfiguration(pathStyleAccess())
                .build();
    }

    private static S3Configuration pathStyleAccess() {
        return S3Configuration.builder().pathStyleAccessEnabled(true).build();
    }

    private static StaticCredentialsProvider credentialsOf(S3Properties properties) {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(properties.accessKeyId(), properties.secretAccessKey()));
    }
}
