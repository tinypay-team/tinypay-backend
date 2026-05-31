package com.tinypay.auth.jwt;

import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessExpiration))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshExpiration))
                .signWith(secretKey)
                .compact();
    }

    public Long extractUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    public void validateRefreshTokenOrThrow(String refreshToken) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorType.INVALID_REFRESH_TOKEN);
        } catch (MalformedJwtException e) {
            throw new CustomException(ErrorType.INVALID_TOKEN_FORMAT);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorType.MISSING_REFRESH_TOKEN);
        }
    }

    public void validateAccessTokenOrThrow(String accessToken) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(accessToken);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorType.INVALID_ACCESS_TOKEN);
        } catch (MalformedJwtException e) {
            throw new CustomException(ErrorType.INVALID_TOKEN_FORMAT);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorType.MISSING_ACCESS_TOKEN);
        }
    }
}