package com.nowakartur97.personalkanbanboardbackend.auth;

import com.nowakartur97.personalkanbanboardbackend.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.graphql.server.WebSocketGraphQlInterceptor;
import org.springframework.graphql.server.WebSocketGraphQlRequest;
import org.springframework.graphql.server.WebSocketSessionInfo;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JWTWebSocketInterceptor implements WebSocketGraphQlInterceptor {

    private final static String AUTHENTICATION_ATTRIBUTE = "authentication";

    private final JWTUtil jwtUtil;
    private final UserService userService;


    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        if (request instanceof WebSocketGraphQlRequest webSocketGraphQlRequest) {
            Authentication auth = (Authentication) webSocketGraphQlRequest.getSessionInfo()
                    .getAttributes()
                    .get(AUTHENTICATION_ATTRIBUTE);
            if (auth != null) {
                return chain.next(request).contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
            }
        }
        return chain.next(request);
    }

    @Override
    public Mono<Object> handleConnectionInitialization(WebSocketSessionInfo sessionInfo,
                                                       Map<String, Object> connectionInitPayload) {
        String authHeader = (String) jwtUtil.getAuthorizationHeader(connectionInitPayload);
        if (jwtUtil.isBearerTypeAuthorization(authHeader)) {
            String authToken = jwtUtil.getJWTFromHeader(authHeader);
            String username = jwtUtil.extractUsername(authToken);
            return userService.findByUsernameForAuthentication(username)
                    .doOnNext(user -> {
                        Authentication auth = new UsernamePasswordAuthenticationToken(user.getUsername(), authToken,
                                List.of(new SimpleGrantedAuthority(user.getRole().name())));
                        sessionInfo.getAttributes().put(AUTHENTICATION_ATTRIBUTE, auth);
                    })
                    .thenReturn(connectionInitPayload);
        }
        return Mono.just(connectionInitPayload);
    }
}
