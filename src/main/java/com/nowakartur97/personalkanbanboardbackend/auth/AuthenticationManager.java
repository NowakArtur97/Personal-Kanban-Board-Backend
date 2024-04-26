package com.nowakartur97.personalkanbanboardbackend.auth;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil.ROLE_CLAIM;

@Component
@RequiredArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final JWTUtil jwtUtil;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString().trim();
        String username = jwtUtil.extractUsername(token);
        return Mono.just(jwtUtil.isTokenValid(token, username))
                .filter(isTokenValid -> isTokenValid)
                .switchIfEmpty(Mono.empty())
                .map(isTokenValid -> {
                    Claims claims = jwtUtil.extractAllClaims(token);
                    List<String> rolesMap = claims.get(ROLE_CLAIM, List.class);
                    return new UsernamePasswordAuthenticationToken(
                            username,
                            token,
                            rolesMap.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                    );
                });
    }
}
