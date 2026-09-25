package com.univgo.backend.shared.config;

import com.univgo.backend.auth.infrastructure.adapter.out.security.JwtAuthenticationFilter;
import com.univgo.backend.shared.infrastructure.CsrfCookieFilter;
import com.univgo.backend.shared.infrastructure.RestAccessDeniedHandler;
import com.univgo.backend.shared.infrastructure.RestAuthenticationEntryPoint;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /** {@code /auth/me} is deliberately absent: identifying the session requires having one. */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/login",
            "/auth/refresh",
            "/auth/logout",
            "/ping",
            // Boot's own error controller: an unhandled exception on an authenticated endpoint
            // forwards here internally, and without this the entry point overwrites the real
            // status/message with a misleading 401 before it ever reaches the client.
            "/error",
            "/swagger-ui/**",
            "/swagger-dark.css",
            "/v3/api-docs/**"
    };

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CsrfCookieFilter csrfCookieFilter,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler) {
        try {
            http
                    // Auth rides a cookie (see AuthCookieProperties), so a forged cross-site request
                    // carries it exactly like a real one. The double-submit cookie below is what
                    // stops that: the browser can attach XSRF-TOKEN automatically, but only a script
                    // running on our own origin can ever read its value back out to echo as the
                    // X-XSRF-TOKEN header, which is the part a forged request can't fake.
                    // /auth/login is the one exemption: it is what creates the session every other
                    // endpoint's token check depends on, so there is nothing yet to double-submit.
                    .csrf(csrf -> csrf
                            // NOSONAR java:S3330 — this cookie carries no secret, only a CSRF nonce
                            // a same-origin script must be able to read to echo it as X-XSRF-TOKEN.
                            // HttpOnly would make that echo impossible and disable the check above it
                            // exists to run. The session/refresh cookies that actually authenticate
                            // stay HttpOnly (see AuthCookieFactory) and are untouched by this.
                            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                            // NOSONAR java:S4502 — no session exists yet at this endpoint, so there
                            // is nothing yet to double-submit against; every endpoint that acts on an
                            // established session stays covered above. The one residual risk here is
                            // login CSRF (a forged request logs the victim's browser into an account
                            // the attacker already controls, using the attacker's own credentials) —
                            // it cannot expose or act as the victim's real account, unlike the CSRF
                            // this configuration exists to stop everywhere else.
                            .ignoringRequestMatchers("/auth/login"))
                    .cors(Customizer.withDefaults())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .exceptionHandling(handling -> handling
                            .authenticationEntryPoint(authenticationEntryPoint)
                            .accessDeniedHandler(accessDeniedHandler))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                            .anyRequest().authenticated())
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    // Must run after Spring Security's own CsrfFilter (fixed at its canonical
                    // position regardless of where BasicAuthenticationFilter itself sits, since
                    // httpBasic() is never enabled) so the token it resolves is the one CsrfFilter
                    // just generated for this request.
                    .addFilterAfter(csrfCookieFilter, BasicAuthenticationFilter.class);
            return http.build();
        } catch (Exception ex) {
            throw new SecurityConfigurationException("Failed to build the security filter chain", ex);
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.allowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-XSRF-TOKEN"));
        // Without this the browser drops the session cookie on every cross-origin call.
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
