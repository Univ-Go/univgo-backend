package com.univgo.backend.shared.infrastructure;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * Security rejections happen before any controller runs, so they bypass
 * {@link GlobalExceptionHandler}. This keeps their bodies in the same shape, which is what lets a
 * client treat every failure the same way.
 *
 * <p>The JSON is written by hand rather than through an {@code ObjectMapper}: both fields are
 * constants defined in this package, so there is nothing to escape, and the responder stays
 * independent of which Jackson generation the framework happens to autoconfigure.
 */
final class ApiErrorResponder {

    private ApiErrorResponder() {
    }

    static void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"timestamp\":\"%s\",\"status\":%d,\"message\":\"%s\"}"
                        .formatted(Instant.now(), status.value(), message));
    }
}
