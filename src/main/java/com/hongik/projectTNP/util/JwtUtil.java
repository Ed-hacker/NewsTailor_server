package com.hongik.projectTNP.util;

import com.hongik.projectTNP.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 위한 유틸리티 클래스
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret:DefaultSecretKeyStringForJwtTNPProjectLongEnoughToMeetRequirement}")
    private String secretKeyString;

    @Value("${jwt.expirationMs:3600000}")
    private long validityInMilliseconds;

    private Key secretKey;

    @PostConstruct
    protected void init() {
        if (secretKeyString.getBytes().length < 32) {
            log.warn("Provided JWT secret key string is too short (less than 32 bytes). Using a default secure key.");
        }
        secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }

    /**
     * JWT 토큰을 생성합니다.
     *
     * @param username 사용자 이름
     * @return 생성된 JWT 토큰
     */
    public String generateToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰이 유효한지 검증합니다.
     *
     * @param token JWT 토큰
     * @return 유효성 여부
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }

    /**
     * 토큰으로부터 이름을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 추출된 이름
     */
    public String getUsernameFromToken(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
        } catch (Exception e) {
            log.error("Error parsing JWT token: {}", e.getMessage());
            return null;
        }
    }

    public Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        } catch (Exception e) {
            log.error("Error parsing JWT token claims: {}", e.getMessage());
            return null;
        }
    }
} 