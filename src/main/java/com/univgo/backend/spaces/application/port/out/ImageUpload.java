package com.univgo.backend.spaces.application.port.out;

/**
 * An uploaded file as the application sees it. Exists so {@code MultipartFile} — a {@code
 * spring-web} type — stops at the controller instead of reaching a use case.
 */
public record ImageUpload(String filename, String contentType, byte[] bytes) {
}
