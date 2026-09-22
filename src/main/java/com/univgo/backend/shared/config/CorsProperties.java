package com.univgo.backend.shared.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Origins allowed to call the API with credentials. A wildcard is not an option here: the CORS spec
 * rejects {@code *} together with {@code Access-Control-Allow-Credentials}, which cookie auth needs.
 */
@ConfigurationProperties("cors")
public record CorsProperties(List<String> allowedOrigins) {
}
