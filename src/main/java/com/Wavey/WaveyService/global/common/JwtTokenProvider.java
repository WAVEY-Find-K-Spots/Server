package com.Wavey.WaveyService.global.common;

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
import java.util.Date;

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
        return createToken(provider, providerId, accessTokenExpiration);
    }

    public String createRefreshToken(String provider, String providerId) {
        return createToken(provider, providerId, refreshTokenExpiration);
    }

    private String createToken(String provider, String providerId, long expiration) {
        Claims claims = Jwts.claims().setSubject(providerId);
        claims.put("provider", provider);

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //필터에서 사용하는 검증 메서드 (에러 코드를 Request에 담음)
    public boolean validateToken(String token, HttpServletRequest request) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException | SignatureException e) {
            if (request != null) request.setAttribute("exception", ErrorCode.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            if (request != null) request.setAttribute("exception", ErrorCode.EXPIRED_TOKEN);
        } catch (Exception e) {
            if (request != null) request.setAttribute("exception", ErrorCode.INVALID_TOKEN);
        }
        return false;
    }

    //컨트롤러(refresh) 등에서 사용하는 단순 검증 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}