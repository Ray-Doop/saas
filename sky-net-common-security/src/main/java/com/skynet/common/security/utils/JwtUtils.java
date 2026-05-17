package com.skynet.common.security.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Slf4j
public class JwtUtils {

    private static final String SECRET = "SmartCloudJwtSecretKey2024Base64EncodedStringMustBeLongEnough";
    private static final long EXPIRE_SECONDS = 7200L;

    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                "U21hcnRDbG91ZEp3dFNlY3JldEtleTIwMjRCYXNlNjRFbmNvZGVkU3RyaW5nTXVzdEJlTG9uZ0Vub3VnaA=="));
    }

    public static String createToken(Long userId, String username) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_SECONDS * 1000))
                .signWith(getKey())
                .compact();
    }

    public static String createToken(Long userId, String username, Map<String, Object> extraClaims) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claims(extraClaims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_SECONDS * 1000))
                .signWith(getKey())
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static Long getUserId(String token) {
        return Long.valueOf(parseToken(token).getSubject());
    }

    public static String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }

    public static boolean isExpired(String token) {
        try {
            parseToken(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            return true;
        }
    }

    public static boolean validate(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            log.warn("JWT校验失败: {}", e.getMessage());
            return false;
        }
    }
}
