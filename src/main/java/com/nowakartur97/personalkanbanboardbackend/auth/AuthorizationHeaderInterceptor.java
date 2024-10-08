package com.nowakartur97.personalkanbanboardbackend.auth;

import io.micrometer.common.util.StringUtils;
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
        String authHeader = request.getHeaders().getFirst(jwtConfigurationProperties.getAuthorizationHeader());
        if (StringUtils.isNotBlank(authHeader) && jwtUtil.isBearerTypeAuthorization(authHeader)) {
            String token = jwtUtil.getJWTFromHeader(authHeader);
            request.configureExecutionInput((executionInput, builder) ->
                    builder.graphQLContext(Collections.singletonMap(TOKEN_IN_CONTEXT, token)).build());
        }
        return chain.next(request);
    }
}
