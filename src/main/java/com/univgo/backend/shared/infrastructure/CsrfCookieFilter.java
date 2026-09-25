package com.univgo.backend.shared.infrastructure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * {@code CookieCsrfTokenRepository} only writes the {@code XSRF-TOKEN} cookie once something reads
 * the deferred {@link CsrfToken} it attaches to the request; nothing else in the chain does that for
 * a pure REST API with no server-rendered form. Reading it here on every request is what actually
 * puts the cookie on the response, which a browser-based client needs before it can echo the token
 * back as {@code X-XSRF-TOKEN}.
 */
@Component
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
