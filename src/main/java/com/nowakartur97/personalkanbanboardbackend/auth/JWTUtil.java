package com.nowakartur97.personalkanbanboardbackend.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JWTUtil {

    public static final String ROLE_CLAIM = "role";

    private final JWTConfigurationProperties jwtConfigurationProperties;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(jwtConfigurationProperties.getSecretKey().getBytes());
    }

    public String generateToken(String username, String userRole) {
        Map<String, Object> claims = Map.of(ROLE_CLAIM, List.of(userRole));
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtConfigurationProperties.getExpirationTimeInMilliseconds()))
                .signWith(secretKey)
                .compact();
    }

    String getAuthorizationHeader(ServerWebExchange serverWebExchange) {
        return serverWebExchange.getRequest().getHeaders().getFirst(jwtConfigurationProperties.getAuthorizationHeader());
    }

    boolean isBearerTypeAuthorization(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.startsWith(jwtConfigurationProperties.getAuthorizationType());
    }

    String getJWTFromHeader(String authorizationHeader) {
        return authorizationHeader.substring(jwtConfigurationProperties.getAuthorizationHeaderLength());
    }

    public String extractUsername(String token) {
        String subject = extractClaim(token, Claims::getSubject);
        return subject != null ? subject : "";
    }

    boolean isTokenValid(String token, String username) {
        return (extractUsername(token).equals(username) && !isTokenExpired(token));
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claim = extractAllClaims(token);
        return claimsResolver.apply(claim);
    }

    Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date(System.currentTimeMillis()));
    }

    private Date extractExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
