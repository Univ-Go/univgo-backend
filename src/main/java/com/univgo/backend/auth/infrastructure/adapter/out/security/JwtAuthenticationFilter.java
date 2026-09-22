package com.univgo.backend.auth.infrastructure.adapter.out.security;

import com.univgo.backend.shared.config.AuthCookieProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthCookieProperties cookieProperties;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, AuthCookieProperties cookieProperties) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.cookieProperties = cookieProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null) {
            try {
                Claims claims = jwtTokenProvider.parseClaims(token);
                String userId = claims.getSubject();
                List<?> roleNames = claims.get("roles", List.class);

                Collection<SimpleGrantedAuthority> authorities = roleNames.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();
                var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException ex) {
                log.debug("Rejected access token on {} {}: {}", request.getMethod(), request.getRequestURI(),
                        ex.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * The browser authenticates with the session cookie. The Authorization header stays supported for
     * Swagger and for any caller that is not a browser, and is only consulted when no cookie is
     * present, so a stale header can never shadow a live session.
     */
    private String resolveToken(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, cookieProperties.sessionName());
        if (cookie != null && !cookie.getValue().isBlank()) {
            return cookie.getValue();
        }

        String header = request.getHeader("Authorization");
        return header != null && header.startsWith(BEARER_PREFIX) ? header.substring(BEARER_PREFIX.length()) : null;
    }
}
