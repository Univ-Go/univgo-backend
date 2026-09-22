package com.univgo.backend.auth.infrastructure.adapter.out.security;

import com.univgo.backend.auth.application.port.out.TokenProviderPort;
import com.univgo.backend.auth.domain.InvalidRefreshTokenException;
import com.univgo.backend.users.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider implements TokenProviderPort {

    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final Key signingKey;
    private final long accessExpirationMillis;
    private final long refreshExpirationMillis;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long accessExpirationMillis,
            @Value("${jwt.refresh-expiration}") long refreshExpirationMillis) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMillis = accessExpirationMillis;
        this.refreshExpirationMillis = refreshExpirationMillis;
    }

    @Override
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + accessExpirationMillis);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim(CLAIM_ROLES, user.getRoles())
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(signingKey)
                .compact();
    }

    @Override
    public IssuedToken generateRefreshToken(User user) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + refreshExpirationMillis);

        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(signingKey)
                .compact();

        return new IssuedToken(token, expiresAt.toInstant());
    }

    @Override
    public long getAccessExpirationSeconds() {
        return accessExpirationMillis / 1000;
    }

    @Override
    public long getRefreshExpirationSeconds() {
        return refreshExpirationMillis / 1000;
    }

    @Override
    public UUID verifyRefreshToken(String token) {
        try {
            Claims claims = parseClaims(token);
            if (!TYPE_REFRESH.equals(claims.get(CLAIM_TYPE))) {
                throw new InvalidRefreshTokenException();
            }
            return UUID.fromString(claims.getSubject());
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidRefreshTokenException();
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
