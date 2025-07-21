package com.nowakartur97.personalkanbanboardbackend.auth;

import com.nowakartur97.personalkanbanboardbackend.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SecurityContextRepository implements ServerSecurityContextRepository {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final UserService userService;

    @Override
    public Mono<Void> save(ServerWebExchange swe, SecurityContext sc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange serverWebExchange) {
        return Mono.justOrEmpty(jwtUtil.getAuthorizationHeader(serverWebExchange))
                .filter(jwtUtil::isBearerTypeAuthorization)
                .flatMap(authHeader -> {
                    String authToken = jwtUtil.getJWTFromHeader(authHeader);
                    String username = jwtUtil.extractUsername(authToken);
                    return userService.findByUsernameForAuthentication(username).flatMap(user -> {
                        Authentication auth = new UsernamePasswordAuthenticationToken(user.getUsername(), authToken,
                                List.of(new SimpleGrantedAuthority(user.getRole().name())));
                        return authenticationManager.authenticate(auth).map(SecurityContextImpl::new);
                    });
                });
    }
}
