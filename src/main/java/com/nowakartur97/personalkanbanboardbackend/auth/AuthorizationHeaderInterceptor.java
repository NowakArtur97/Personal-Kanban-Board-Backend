package com.nowakartur97.personalkanbanboardbackend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class AuthorizationHeaderInterceptor implements WebGraphQlInterceptor {

    public static final String TOKEN_IN_CONTEXT = "token";

    private final JWTUtil jwtUtil;
    private final JWTConfigurationProperties jwtConfigurationProperties;

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        String header = request.getHeaders().getFirst(jwtConfigurationProperties.getAuthorizationHeader());
        String token = jwtUtil.getJWTFromHeader(header);
        request.configureExecutionInput((executionInput, builder) ->
                builder.graphQLContext(Collections.singletonMap(TOKEN_IN_CONTEXT, token)).build());
        return chain.next(request);
    }
}
