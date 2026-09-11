package com.Wavey.WaveyService.global.common;

import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component; // 이 임포트 확인

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Component // <--- 이 어노테이션이 있는지 꼭 확인하세요!
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private Key key;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(String provider, String providerId) {
        return createToken(provider, providerId, TokenType.ACCESS, accessTokenExpiration);
    }

    public String createRefreshToken(String provider, String providerId) {
        return createToken(provider, providerId, TokenType.REFRESH, refreshTokenExpiration);
    }

    private String createToken(String provider, String providerId, TokenType tokenType, long expiration) {
        Claims claims = Jwts.claims().setSubject(providerId);
        claims.put("provider", provider);
        claims.put("tokenType", tokenType.name());

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //필터에서 사용하는 검증 메서드 (에러 코드를 Request에 담음)
    public boolean validateToken(String token, HttpServletRequest request) {
        return validateToken(token, request, TokenType.ACCESS);
    }

    public boolean validateToken(String token, HttpServletRequest request, TokenType expectedType) {
        try {
            Claims claims = parseClaims(token);
            return expectedType.name().equals(claims.get("tokenType", String.class));
        } catch (MalformedJwtException | SignatureException e) {
            if (request != null) request.setAttribute("exception", ErrorCode.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            if (request != null) request.setAttribute("exception", ErrorCode.EXPIRED_TOKEN);
        } catch (Exception e) {
            if (request != null) request.setAttribute("exception", ErrorCode.INVALID_TOKEN);
        }
        return false;
    }

    public Claims getClaims(String token) {
        return parseClaims(token);
    }

    public Claims getValidatedClaims(String token, TokenType expectedType) {
        try {
            Claims claims = parseClaims(token);
            if (!expectedType.name().equals(claims.get("tokenType", String.class))) {
                throw new CustomException(ErrorCode.INVALID_TOKEN);
            }
            return claims;
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (CustomException e) {
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    public Duration getRemainingValidity(Claims claims) {
        long remainingMillis = claims.getExpiration().getTime() - System.currentTimeMillis();
        return Duration.ofMillis(Math.max(remainingMillis, 0));
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
