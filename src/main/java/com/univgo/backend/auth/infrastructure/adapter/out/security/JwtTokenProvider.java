package com.univgo.backend.auth.infrastructure.adapter.out.security;

import com.univgo.backend.auth.application.port.out.TokenProviderPort;
import com.univgo.backend.users.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider implements TokenProviderPort {

    private static final String CLAIM_ROLES = "roles";

    private final Key signingKey;
    private final long expirationMillis;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMillis) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    @Override
    public String generateToken(User user) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim(CLAIM_ROLES, user.getRoles())
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(signingKey)
                .compact();
    }

    @Override
    public long getExpirationSeconds() {
        return expirationMillis / 1000;
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
