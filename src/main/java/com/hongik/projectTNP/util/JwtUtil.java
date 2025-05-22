package com.hongik.projectTNP.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * JWT 토큰 생성 및 검증을 위한 유틸리티 클래스
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:default_secret_key_which_is_at_least_32_bytes_long}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24시간 (밀리초)
    private long expiration;

    /**
     * JWT 토큰을 생성합니다.
     *
     * @param email 사용자 이메일
     * @return 생성된 JWT 토큰
     */
    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        Key key = Keys.hmacShaKeyFor(secret.getBytes());

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 토큰으로부터 이메일을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 추출된 이메일
     */
    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 토큰이 유효한지 검증합니다.
     *
     * @param token JWT 토큰
     * @return 유효성 여부
     */
    public boolean validateToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(secret.getBytes());
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토큰에서 클레임을 추출합니다.
     *
     * @param token JWT 토큰
     * @param claimsResolver 클레임 처리 함수
     * @param <T> 반환 타입
     * @return 추출된 클레임 값
     */
    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    /**
     * 토큰의 만료 여부를 확인합니다.
     *
     * @param token JWT 토큰
     * @return 만료 여부
     */
    private boolean isTokenExpired(String token) {
        Date expiration = getClaimFromToken(token, Claims::getExpiration);
        return expiration.before(new Date());
    }
} 