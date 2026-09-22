package com.univgo.backend.shared.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Answers 401 rather than Spring's default for an anonymous request. The distinction is load-bearing
 * on the client: 401 means "renew the session and retry", 403 means "this is not your role" and must
 * never trigger a refresh.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        ApiErrorResponder.write(response, HttpStatus.UNAUTHORIZED, "Authentication required");
    }
}
